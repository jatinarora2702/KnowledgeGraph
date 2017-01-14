import codecs
import pickle
import subprocess


DATA_PATH = '../../Data/Reviews/Jindal-Liu/'


def get_tokenized_sentences(tokfile):
	f = codecs.open(DATA_PATH + tokfile, 'r', 'utf-8')
	toksentlist = list()
	toksent = list()
	for line in f:
		if line == '\n':
			toksentlist.append(toksent)
			toksent = list()
		else:
			toksent.append(line.split('\t')[1])
	return toksentlist


def save_as_pickle(toksentlist, picklefile):
	picklefile = DATA_PATH + picklefile
	with open(picklefile, 'wb') as handle:
		pickle.dump(toksentlist, handle)


def main():
	picklefile = DATA_PATH + 'labelled2Pk.pickle'
	with open(picklefile, 'rb') as handle:
		rev_list = pickle.load(handle)
	for rev in rev_list:
		for sent in rev['review/labels']:
			print(sent['sentence'])

	toksentlist = get_tokenized_sentences('one-word-per-line.txt')
	save_as_pickle(toksentlist, 'toksentsPk.pickle')


if __name__ == '__main__':
	main()
