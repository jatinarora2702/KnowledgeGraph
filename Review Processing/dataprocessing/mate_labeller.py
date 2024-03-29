import codecs
import store

def process_sentence(sent):
	processed_sentence = list()
	for term in sent:
		vec = term.split('\t')
		vec[-1] = vec[-1][:-1]
		processed_sentence.append(vec)
	return processed_sentence


def write_to_file(filename, sent):
	f = codecs.open(store.DATA_PATH + filename, 'a', encoding='utf-8')
	for term in sent:
		f.write(term)
	f.write("\n")


def format_sentence(sent):
	final_sent = list()
	for term in sent:
		line = ''
		n = len(term)
		for i in range(n-1):
			line += term[i] + '\t'
		line += term[n-1] + '\n'
		final_sent.append(line)
	return final_sent


def mark_sentence(sent, marked):
	newsent = list()
	n = len(sent)
	for i in range(n):
		if i not in marked:
			templist = sent[i]
			templist.append('_')
			newsent.append(templist)
		else:
			newsent.append(sent[i])
	return newsent


def add_to_term(sent, index, markstr):
	newsent = list()
	i = 0
	for line in sent:
		if i == index:
			templist = sent[i]
			templist.append(markstr)
			newsent.append(templist)
		else:
			newsent.append(sent[i])
		i += 1
	return newsent
	

def modify_term(sent, index, type_num):
	newsent = list()
	i = 0
	for line in sent:
		if i == index:
			templist = list()
			j = 0
			for term in sent[i]:
				if j == 12:
					templist.append('Y')
				elif j == 13:
					templist.append('comparative.0' + str(type_num))
				else:
					templist.append(term)
				j += 1
			newsent.append(templist)
		else:
			newsent.append(sent[i])
		i += 1
	return newsent


def do_labelling(sent, sentdict, outfilename):
	sent = process_sentence(sent)
	for preddict in sentdict['details']:
		sent = modify_term(sent, preddict['predicate'], preddict['type'])
		sent = add_to_term(sent, preddict['aspect'], 'A0')
		sent = add_to_term(sent, preddict['prod1'], 'A1')
		sent = add_to_term(sent, preddict['prod2'], 'A2')
		marked = list()
		marked.append(preddict['aspect'])
		marked.append(preddict['prod1'])
		marked.append(preddict['prod2'])
		sent = mark_sentence(sent, marked)
	sent = format_sentence(sent)
	write_to_file(outfilename, sent)


def main():
	f = codecs.open(store.DATA_PATH + 'labelled-all-sentences.parsed.txt', 'r', 'utf-8')
	rev_list = store.get_reviews_list_pickle('labelledAll.pickle')
	sent_list = store.get_labelled_sentences(rev_list)
	i = 0
	sent = list()
	for line in f:
		if line == '\n':
			do_labelling(sent, sent_list[i], 'mate-all-labelled.txt')
			i += 1
			sent = list()
		else:
			sent.append(line)


if __name__ == '__main__':
	main()
