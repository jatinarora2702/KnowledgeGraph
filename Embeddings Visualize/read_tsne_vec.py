import pickle
import codecs


def main():
	f1 = codecs.open('tmp1.txt', 'w', 'utf-8')
	f2 = codecs.open('tmp2.txt', 'w', 'utf-8')
	with open('tsne_word2vec-5-50.pickle', 'rb') as handle:
		Y = pickle.load(handle)
	vec = Y[:, 0]
	for elem in vec:
		print >> f1, elem  
	vec = Y[:, 1]
	for elem in vec:
		print >> f2, elem

if __name__ == '__main__':
	main()
