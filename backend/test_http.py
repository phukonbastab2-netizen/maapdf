import json, threading, unittest
from urllib.request import Request, urlopen
from urllib.error import HTTPError
from http.server import ThreadingHTTPServer
from server import Handler

class HttpTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.server = ThreadingHTTPServer(('127.0.0.1', 0), Handler)
        cls.thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.thread.start()
        cls.url = 'http://127.0.0.1:' + str(cls.server.server_port)
    @classmethod
    def tearDownClass(cls):
        cls.server.shutdown()
        cls.server.server_close()
        cls.thread.join()
    def test_health(self):
        with urlopen(self.url+'/health') as r:
            self.assertEqual(json.load(r)['status'], 'ok')
    def test_intent(self):
        request = Request(self.url+'/intent', data=json.dumps({'text':'replace A with B'}).encode(), headers={'Content-Type':'application/json'})
        with urlopen(request) as r:
            self.assertEqual(json.load(r), {'action':'replace','from':'A','to':'B'})
    def test_malformed(self):
        with self.assertRaises(HTTPError) as error:
            urlopen(Request(self.url+'/intent', data=b'{broken'))
        self.assertEqual(error.exception.code, 400)
    def test_too_large(self):
        with self.assertRaises(HTTPError) as error:
            urlopen(Request(self.url+'/intent', data=b'a'*9000))
        self.assertEqual(error.exception.code, 413)
