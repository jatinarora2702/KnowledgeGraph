import pymysql
import nltk
import re
from nltk import tokenize
from bs4 import BeautifulSoup


vec = list()


def check(line, posline):
	for elem in posline:
		if elem[1] in 'RBR' or elem[1] in 'JJR':
			vec.append(line)
			return 1
	return 0


def main():
	db = pymysql.connect("localhost", "root", "sumana", "BTP")
	cursor = db.cursor()
	sql = 'Select ReviewText from comProdRev'
	cursor.execute(sql)
	cnt = 0
	data = cursor.fetchall()
	for line in data:
		line = line[0]
		soup = BeautifulSoup(line, "lxml")
		Review = soup.get_text()
		rev = re.split(r'(?<!\w\.\w.)(?<![A-Z][a-z]\.)(?<=\.|\?|\!)\s', Review)
		for sen in rev:
			tokline = nltk.word_tokenize(sen)
			posline = nltk.pos_tag(tokline)
			cnt += check(sen, posline)
			# print('works')
	print('# comparison based sentences: ' + str(cnt))


if __name__ == '__main__':
	main()