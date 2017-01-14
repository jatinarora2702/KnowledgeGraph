import codecs
import pickle


DATA_PATH = '../../Data/final/final-corrected/'


def get_reviews_list_pickle(picklefile):
    picklefile = DATA_PATH + picklefile
    with open(picklefile, 'rb') as handle:
        rev_list = pickle.load(handle)
    return rev_list


def read_toksents(picklefile):
	picklefile = DATA_PATH + picklefile
	with open(picklefile, 'rb') as handle:
		vec = pickle.load(handle)
	return vec


def make_seq_sent(toklist):
	sent = ''
	for tok in toklist[:-1]:
		sent += tok + ' '
	sent += toklist[-1] + '\n'
	return sent


def make_seq2_sent_list(n):
	toklist = list()
	for i in range(n):
		toklist.append('NONE')
	return toklist


def modify_seq2_sentlist(toklist, tok, pos):
	newtoklist = list()
	if tok == 'type' or (pos >= 0 and pos < len(toklist)):
		newtoklist = toklist[:pos]
		newtoklist.append(tok.upper())
		if pos+1 < len(toklist):
			newtoklist += toklist[pos+1:]
		return newtoklist
	else:
		return toklist


def main():
	fseq1 = codecs.open(DATA_PATH + 'seq1.txt', 'w', 'utf-8')
	fseq2 = codecs.open(DATA_PATH + 'seq2.txt', 'w', 'utf-8')
	revlist = get_reviews_list_pickle('labelledPk.pickle')
	toklist = read_toksents('toksentsPk.pickle')
	cnt = 0
	# print(len(toklist))
	for rev in revlist:
		for sent in rev['review/labels']:
			# print(toklist[cnt])
			seq1sent = make_seq_sent(toklist[cnt])
			seq2sent_list = make_seq2_sent_list(len(toklist[cnt]))
			for preddict in sent['details']:
				for key in preddict:
					seq2sent_list = modify_seq2_sentlist(seq2sent_list, key, preddict[key])
			seq2sent = make_seq_sent(seq2sent_list)
			fseq1.write(seq1sent)
			fseq2.write(seq2sent)
			cnt += 1


if __name__ == '__main__':
	main()
