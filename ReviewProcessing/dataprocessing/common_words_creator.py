import codecs
import pickle
import operator
from nltk import pos_tag
from nltk.tokenize import word_tokenize


DATA_PATH = '../../Data/'


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
	cnt = 0
	f = codecs.open(DATA_PATH + 'Electronics_Dict.txt', 'w', 'utf-8')
	sentlist = get_review_sentences('ElectronicsPk.pickle')
	print("Got Review Sentences!")
	vocab = dict()
	for sent in sentlist:
		toklist = word_tokenize(sent)
		taglist = pos_tag(toklist)
		for elem in taglist:
			if elem[1] == 'NN' or elem[1] == 'NNS' or elem[1] == 'NNP' or elem[1] == 'NNPS':
				if elem[0] in vocab:
					vocab[elem[0]] += 1
				else:
					vocab[elem[0]] = 1
	print("Counted all elements!")
	sorted_vocab = sorted(vocab.items(), key=operator.itemgetter(1), reverse=True)
	print("Sorting Done!")
	for elem in sorted_vocab:
		print(elem[0], file=f)
	print("DONE")


if __name__ == '__main__':
	main()
