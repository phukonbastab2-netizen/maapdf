import unittest
from server import parse
class IntentTests(unittest.TestCase):
    def test_replace(self):
        self.assertEqual(parse('replace Suresh with Ramesh'), {'action':'replace','from':'Suresh','to':'Ramesh'})
    def test_hinglish(self):
        self.assertEqual(parse('Suresh ki jagah Ramesh likho')['to'], 'Ramesh')
    def test_hindi(self):
        self.assertEqual(parse('सुरेश की जगह रमेश लिखो')['to'], 'रमेश')
    def test_page(self):
        self.assertEqual(parse('पेज २ खोलो'), {'action':'page','page':'2'})
    def test_unknown_is_not_edit(self):
        self.assertEqual(parse('give Ramesh 5000 rupees'), {'action':'unknown'})
if __name__ == '__main__':
    unittest.main()
