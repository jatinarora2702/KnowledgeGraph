import codecs
from gensim.models import Word2Vec


DATA_PATH = '.'


def main():
	fw = codecs.open('Electronics_Dict.txt', 'r', 'utf-8')
	fl = codecs.open('labels.txt', 'w', 'utf-8')
	fv = codecs.open('vectors.txt', 'w', 'utf-8')
	Y = Word2Vec.load('word2vec-5-50.bin')
	for line in fw:
		if line[:-1] in Y:
			vec = Y[line[:-1]]
			for elem in vec[:-1]:
				fv.write(str(elem) + '\t')
			fv.write(str(vec[-1]) + '\n')
			fl.write(line)


if __name__ == '__main__':
	main()

