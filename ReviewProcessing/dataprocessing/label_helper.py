import re
import codecs
from nltk import pos_tag
from nltk.tokenize import sent_tokenize, word_tokenize
import store


def ask_ignore():
	inp = int(input("ignore(0) | consider(1): "))
	return True if inp == 0 else False


def print_sentence(sent):
	toklist = word_tokenize(sent)
	i = 1
	for tok in toklist:
		print(str(i) + ". " + tok)
		i += 1


def get_aspect(sent):
	inp = int(input("aspect: ")) - 1
	return inp


def get_entity(num, sent):
	inp = int(input("entity " + str(num) + ": ")) - 1
	return inp


def get_predicate(sent):
	inp = int(input("predicate:")) - 1
	type_num = int(input("type:"))
	return inp, type_num


def label_sentence(sent):
	rev = dict()
	rev['sentence'] = sent
	rev['details'] = list()
	print_sentence(sent)
	ignored = ask_ignore()
	if not ignored:
		morepred = 1
		while morepred == 1:
			done = 0
			while done == 0:
				preddict = dict()
				preddict['predicate'], preddict['type'] = get_predicate(sent)
				preddict['aspect'] = get_aspect(sent)
				preddict['prod1'] = get_entity(1, sent)
				preddict['prod2'] = get_entity(2, sent)
				done = int(input('correct? (0/1):'))
			rev['details'].append(preddict)
			morepred = int(input('more predicates? (0/1):'))
	return rev, not ignored


def has_comparison(sent):
	toklist = word_tokenize(sent)
	poslist = pos_tag(toklist)
	allrelList = ["similar", "better", "more", "faster", "superior", "inferior", "bigger", "smaller", "improved", "larger", "higher", "less", "wider", "greater", "finer", "quicker", "improvement", "same", "identical", "taller", "step up", "nicer", "longer"]
	adjlist = ["bigger", "heavier", "cheaper", "upgraded", "predecessor", "upgrade", "lighter", "larger", "noiser", "faster", "smaller"]

	for entry in poslist:
		if entry[1] == 'RBR' or entry[1] == 'JJR': # or entry[1] == 'JJ' or entry[1] == 'RB':
			return True
		if entry[0] in allrelList or entry[0] in adjlist:
			return True
	return False


def label_review(rev):
    lrev = rev
    labelled_list = list()

    # sent_list = sent_tokenize(rev['review/text'])
    sent_list = re.split(r'(?<!\w\.\w.)(?<![A-Z][a-z]\.)(?<=\.|\?|\!)\s', rev['review/text'])
    for sent in sent_list:
        if has_comparison(sent):
            lsent, f = label_sentence(sent)
            if f:
                labelled_list.append(lsent)

    lrev['review/labels'] = labelled_list
    return lrev


def manual_labelling(rev_filename, labelled_filename, labelled_sentences_filename, picklefile):
    rev_list = store.get_reviews_list(rev_filename)
    for rev in rev_list:
        if 'review/text' in rev:
            labelled_rev = label_review(rev)
            if len(labelled_rev['review/labels']) > 0:
            	store.write_review_to_file(labelled_filename, labelled_sentences_filename, picklefile, labelled_rev)
            print("-------------------------------------")


def main():
	manual_labelling('manual-pc2.txt', 'labelled2.txt', 'labelled2-sentences.txt', 'labelled2Pk.pickle')


if __name__ == '__main__':
	main()
