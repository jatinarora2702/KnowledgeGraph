import codecs


DATA_PATH = '../../Data/all/Unlabelled/'


def sent_length_threshold(inpfile, outfile, threshold):
	inpfile = DATA_PATH + inpfile
	outfile = DATA_PATH + outfile
	fi = codecs.open(inpfile, 'r', 'utf-8')
	fo = codecs.open(outfile, 'w', 'utf-8')
	for sent in fi:
		if len(sent.split(' ')) <= threshold:
			fo.write(sent)


def main():
	for i in range(9, 50):
		sent_length_threshold(i+'.txt', i+'-50.txt', 50)	


if __name__ == '__main__':
	main()
