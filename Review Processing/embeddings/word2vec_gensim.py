import codecs
import pickle
import re
from gensim.models import Word2Vec


DATA_PATH = '../Data/'


def get_reviews_list_pickle(picklefile):
    picklefile = DATA_PATH + picklefile
    with open(picklefile, 'rb') as handle:
        rev_list = pickle.load(handle)
    return rev_list


def get_review_sentences(picklefile):
    rev_list = get_reviews_list_pickle(picklefile)
    print("Read Pickle File..")
    sent_list = list()
    for rev in rev_list:
        if 'review/text' in rev:
            sent_list.append(rev['review/text'])
    return sent_list


def main():
	sentlist = list()
	revlist = get_review_sentences('ElectronicsPk.pickle')
	for Review in revlist:
		rev = re.split(r'(?<!\w\.\w.)(?<![A-Z][a-z]\.)(?<=\.|\?|\!)\s', Review)
		for sent in rev:
			sentlist.append(sent.split(' '))
	model = Word2Vec(sentlist, size=300, window=5, min_count=1, workers=32)
	model.save('word2vec-5-300.bin')


if __name__ == '__main__':
	main()
