import codecs


DATA_PATH = '../../Embedding/Glove-Electronics/'


def main():
	fr = codecs.open(DATA_PATH + 'vectors.txt', 'r', 'utf-8')
	fwl = codecs.open(DATA_PATH + 'glove_labels.txt', 'w', 'utf-8')
	fwv = codecs.open(DATA_PATH + 'glove_vectors.txt', 'w', 'utf-8')
	for line in fr:
		fwl.write(line.split(' ')[0])
		fwv.write(line.split(' ')[1:])


if __name__ == '__main__':
	main()