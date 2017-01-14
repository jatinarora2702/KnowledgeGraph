import codecs


DATA_PATH = '../../Data/all/Unlabelled/parsed/'
# DATA_PATH = '../../Data/all/'


def do_correct(word):
	print(word)
	return word.split('_')[1]
	# index = 0
	# while index < len(word) and not (word[index] >= '0' and word[index] <= '9'):
	# 	index += 1
	# return word[index:]


def cleandata(sent):
	sentlist = sent.split('\t')
	if (not '_' in sentlist[0]) or sentlist[0][0] == '\n':
		return sent
	modsentlist = list()
	modsentlist.append(do_correct(sentlist[0]))
	modsentlist += sentlist[1:]
	modsent = ''
	for elem in modsentlist[:-1]:
		modsent += elem + '\t'
	modsent += modsentlist[-1]
	return modsent


def matecleaner(inpfile, outfile):
	fr = codecs.open(DATA_PATH + inpfile, 'r', 'utf-8')
	fw = codecs.open(DATA_PATH + outfile, 'w', 'utf-8')
	for line in fr:
		fw.write(cleandata(line))


def sentence_length_thresholding(inpfile, outfile, maxlen):
	fr = codecs.open(DATA_PATH + inpfile, 'r', 'utf-8')
	fw = codecs.open(DATA_PATH + outfile, 'w', 'utf-8')
	sent = list()
	cnt = 0
	for line in fr:
		if line == '\n':
			if cnt <= maxlen:
				for elem in sent:
					fw.write(elem)
				fw.write('\n')
			sent = list()
			cnt = 0
		else:
			sent.append(line)
			cnt += 1


def remove_invalid(inpfile, outfile):
	fr = codecs.open(DATA_PATH + inpfile, 'r', 'utf-8')
	fw = codecs.open(DATA_PATH + outfile, 'w', 'utf-8')
	sent = list()
	valid = False
	for line in fr:
		if line == '\n':
			if valid:
				for elem in sent:
					fw.write(elem)
				fw.write('\n')
			sent = list()
			valid = False
		else:
			sent.append(line)
			if 'Y\tcomparative.0' in line:
				valid = True


def get_same_pred_multiple_occur(inpfile):
	fr = codecs.open(DATA_PATH + inpfile, 'r', 'utf-8')
	sent = list()
	cnt = 0
	for line in fr:
		if line == '\n':
			if cnt + 14 < len(sent[0].split('\t')):
				for elem in sent:
					print(elem, end='')
				print()
			sent = list()
			cnt = 0
		else:
			sent.append(line)
			if 'Y\tcomparative.0' in line:
				cnt += 1
 

def main():
	# matecleaner('9.parsed.txt', '9-cleaned.parsed.txt')
	# matecleaner('10.parsed.txt', '10-cleaned.parsed.txt')
	# matecleaner('11.parsed.txt', '11-cleaned.parsed.txt')
	# matecleaner('12.parsed.txt', '12-cleaned.parsed.txt')
	matecleaner('13.parsed.txt', 'cleaned/13.parsed.txt')
	matecleaner('14.parsed.txt', 'cleaned/14.parsed.txt')
	matecleaner('15.parsed.txt', 'cleaned/15.parsed.txt')
	matecleaner('16.parsed.txt', 'cleaned/16.parsed.txt')
	matecleaner('17.parsed.txt', 'cleaned/17.parsed.txt')
	matecleaner('18.parsed.txt', 'cleaned/18.parsed.txt')
	matecleaner('19.parsed.txt', 'cleaned/19.parsed.txt')
	matecleaner('20.parsed.txt', 'cleaned/20.parsed.txt')
	matecleaner('21.parsed.txt', 'cleaned/21.parsed.txt')
	matecleaner('22.parsed.txt', 'cleaned/22.parsed.txt')
	matecleaner('23.parsed.txt', 'cleaned/23.parsed.txt')
	matecleaner('24.parsed.txt', 'cleaned/24.parsed.txt')
	matecleaner('25.parsed.txt', 'cleaned/25.parsed.txt')
	matecleaner('26.parsed.txt', 'cleaned/26.parsed.txt')
	matecleaner('27.parsed.txt', 'cleaned/27.parsed.txt')
	matecleaner('28.parsed.txt', 'cleaned/28.parsed.txt')
	matecleaner('29.parsed.txt', 'cleaned/29.parsed.txt')
	matecleaner('30.parsed.txt', 'cleaned/30.parsed.txt')
	matecleaner('31.parsed.txt', 'cleaned/31.parsed.txt')
	matecleaner('32.parsed.txt', 'cleaned/32.parsed.txt')
	matecleaner('33.parsed.txt', 'cleaned/33.parsed.txt')
	matecleaner('34.parsed.txt', 'cleaned/34.parsed.txt')
	matecleaner('35.parsed.txt', 'cleaned/35.parsed.txt')
	matecleaner('36.parsed.txt', 'cleaned/36.parsed.txt')
	matecleaner('37.parsed.txt', 'cleaned/37.parsed.txt')
	matecleaner('38.parsed.txt', 'cleaned/38.parsed.txt')
	matecleaner('39.parsed.txt', 'cleaned/39.parsed.txt')
	matecleaner('40.parsed.txt', 'cleaned/40.parsed.txt')
	matecleaner('41.parsed.txt', 'cleaned/41.parsed.txt')
	matecleaner('42.parsed.txt', 'cleaned/42.parsed.txt')
	matecleaner('43.parsed.txt', 'cleaned/43.parsed.txt')
	matecleaner('44.parsed.txt', 'cleaned/44.parsed.txt')
	matecleaner('45.parsed.txt', 'cleaned/45.parsed.txt')
	matecleaner('46.parsed.txt', 'cleaned/46.parsed.txt')
	matecleaner('47.parsed.txt', 'cleaned/47.parsed.txt')
	matecleaner('48.parsed.txt', 'cleaned/48.parsed.txt')
	matecleaner('49.parsed.txt', 'cleaned/49.parsed.txt')
	# sentence_length_thresholding('5-cleaned.parsed.txt', 'cleaned/5.parsed.txt', 50)
	# remove_invalid('mate-labelled-final.txt', 'mate-labelled-final-pred.txt')
	# get_same_pred_multiple_occur('mate-all-labelled.txt')


if __name__ == '__main__':
	main()
