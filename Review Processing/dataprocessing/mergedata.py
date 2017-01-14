import pickle

DATA_PATH = '../../Data/all/'

def get_reviews_list_pickle(picklefile):
    picklefile = DATA_PATH + picklefile
    with open(picklefile, 'rb') as handle:
        rev_list = pickle.load(handle)
    return rev_list

def store_as_pickle(revlist, picklefile):
    picklefile = DATA_PATH + picklefile
    with open(picklefile, 'wb') as handle:
        pickle.dump(revlist, handle)


def get_sentences(picklefile):
	revs = get_reviews_list_pickle(picklefile)
	for rev in revs:
		for sent in rev['review/labels']:
			print(sent['sentence'])


def get_sentence_index(picklefile):
	revs = get_reviews_list_pickle(picklefile)
	cnt = 0
	for rev in revs:
		for sent in rev['review/labels']:
			cnt += 1
			if 'much better camera in my hands' in sent['sentence']:
				print(cnt)


def make_lists(revlist):
	for rev in revlist:
		for sent in rev['review/labels']:
			for preddict in sent['details']:
				preddict['prod1'] = [preddict['prod1']]
				preddict['prod2'] = [preddict['prod2']]
				preddict['aspect'] = [preddict['aspect']]
	return revlist


def main():
	revlist1 = get_reviews_list_pickle('labelledPk.pickle')
	revlist1 = make_lists(revlist1)
	revlist2 = get_reviews_list_pickle('kesslerLabelled.pickle')
	revlist3 = get_reviews_list_pickle('kesslerLabelled1.pickle')
	revlist = revlist2 + revlist3 + revlist1
	store_as_pickle(revlist, 'labelledAll.pickle')
	# get_sentences('labelledAll.pickle')
	# get_sentence_index('labelled2Pk.pickle')

if __name__ == '__main__':
	main()
