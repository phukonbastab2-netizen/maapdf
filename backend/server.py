"""Zero-dependency CPU intent service. No documents, tokens or paid APIs."""
import json, re, os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

def parse(text):
    text = text.strip()
    match = re.fullmatch(r'replace (.+?) with (.+)', text, re.I)
    if not match:
        match = re.fullmatch(r'(.+?) (?:ki jagah|की जगह) (.+?)(?: likho| लिखो)?', text, re.I)
    if match:
        return {'action': 'replace', 'from': match[1], 'to': match[2]}
    match = re.search(r'(?:page|पेज)\s*(\d+)', text, re.I)
    if match:
        return {'action': 'page', 'page': str(int(match[1]))}
    for action, words in [('next', ['next', 'agla', 'अगला']), ('previous', ['previous', 'pichla', 'पिछला']), ('read', ['read', 'padho', 'पढ़ो', 'सुनाओ']), ('save', ['save', 'सेव', 'सहेज']), ('open', ['open', 'kholo', 'खोलो'])]:
        if any(word in text.lower() for word in words):
            return {'action': action}
    return {'action': 'unknown'}

class Handler(BaseHTTPRequestHandler):
    def log_message(self, *args):
        pass  # Do not log employees' commands.
    def reply(self, status, value):
        data = json.dumps(value, ensure_ascii=False).encode()
        self.send_response(status)
        self.send_header('Content-Type', 'application/json; charset=utf-8')
        self.send_header('Content-Length', str(len(data)))
        self.end_headers()
        self.wfile.write(data)
    def do_GET(self):
        self.reply(200 if self.path == '/health' else 404, {'status': 'ok', 'engine': 'rules-cpu-v1'} if self.path == '/health' else {'error': 'not found'})
    def do_POST(self):
        if self.path != '/intent':
            return self.reply(404, {'error': 'not found'})
        try:
            self.connection.settimeout(5)
            size = int(self.headers.get('Content-Length', '0'))
            if not 0 < size <= 8192:
                return self.reply(413, {'error': 'request size'})
            data = json.loads(self.rfile.read(size))
            text = data.get('text')
            if not isinstance(text, str) or not 0 < len(text) <= 2000:
                return self.reply(400, {'error': 'text required, max 2000 characters'})
            self.reply(200, parse(text))
        except (ValueError, TypeError, AttributeError):
            self.reply(400, {'error': 'invalid JSON'})

if __name__ == '__main__':
    ThreadingHTTPServer(('0.0.0.0', int(os.environ.get('PORT', '8000'))), Handler).serve_forever()
