import codecs


DATA_PATH = '../../Data/Reviews/Jindal-Liu/'


def main():
	filename = 'sentences-jindal.txt'
	filename = DATA_PATH + filename
	vec = list()
	f = codecs.open(filename, 'r', 'utf-8')
	for line in f:
		vec.append(line)
	newvec = list()
	for elem in vec:
		if elem not in newvec:
			newvec.append(elem)
	for elem in newvec:
		print(elem[:-1])


if __name__ == '__main__':
	main()
