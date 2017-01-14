import pickle
import codecs


DATA_PATH = '../../Data/Reviews/Kessler/'


def getrevs(filename):
	filename = DATA_PATH + filename
	origrevs = dict()
	f = codecs.open(filename, 'r', 'utf-8')
	labelnum = -99
	flag = 1
	for line in f:
		if flag == 0:
			if line == '\n':
				flag = 1
			else:
				rev += line
		else:
			if labelnum >= 0:
				origrevs[labelnum] = rev
			labelnum = int(line)
			rev = ''
			flag = 0
	return origrevs


def getallsents(filename):
	filename = DATA_PATH + filename
	allsent = dict()
	f = codecs.open(filename, 'r', 'utf-8')
	for line in f:
		vec = line.split('\t')
		allsent[vec[0]] = vec[1]
	return allsent


def getelems(wordlist):
	wordlist = wordlist[1:-1]
	vec = wordlist.split(' ')
	newvec = list()
	for elem in vec:
		if (elem.split('_')[0]).isdigit():
			newvec.append(int(elem.split('_')[0])-1)
	return newvec


def processpred(elem):
	vec = elem.split('; ')
	preddict = dict()
	if 'RANKED' in vec[0] or 'DIFFERENCE' in vec[0]:
		preddict['type'] = 1
	elif 'EQUATIVE' in vec[0]:
		preddict['type'] = 2
	elif 'SUPERLATIVE' in vec[0]:
		preddict['type'] = 3
	preddict['prod1'] = getelems(vec[1])
	preddict['prod2'] = getelems(vec[2])
	preddict['aspect'] = getelems(vec[3])
	preddict['predicate'] = int(vec[5].split('_')[0])-1
	return preddict


def parseline(line, allsent):
	detline = dict()
	vec = line.split('\t')[:-1]
	detline['sentnum'] = vec[0]
	detline['revnum'] = int(vec[0].split('-')[0])
	vec = vec[2:]
	predlist = list()
	for elem in vec:
		predlist.append(processpred(elem))
	detline['details'] = predlist
	return detline


def getannotations(filename, origrevs, allsent, sentfile):
	filename = DATA_PATH + filename
	sentfile = DATA_PATH + sentfile
	revlist = list()
	f = codecs.open(filename, 'r', 'utf-8')
	sf = codecs.open(sentfile, 'w', 'utf-8')
	num = -99
	dets = list()
	for line in f:
		detline = parseline(line, allsent)
		# print(detline)
		# print('--------------------------------------------------------------')
		if detline['revnum'] != num:
			if num >= 0:
				rev = dict()
				rev['review/text'] = origrevs[num]
				rev['review/labels'] = dets
				# print(rev)
				revlist.append(rev)
				dets = list()
			num = detline['revnum']
		vec = dict()
		vec['sentence'] = allsent[detline['sentnum']]
		sf.write(vec['sentence'])
		vec['details'] = detline['details']
		dets.append(vec)
	if num >= 0:
		rev = dict()
		rev['review/text'] = origrevs[num]
		rev['review/labels'] = dets
		# print(rev)
		revlist.append(rev)
		dets = list()
	return revlist


def savepickle(revlist, picklefile):
	picklefile = DATA_PATH + picklefile
	with open(picklefile, 'wb') as handle:
		pickle.dump(revlist, handle)


def main():
	origrevs = getrevs('cameras.allsentences.details.txt')
	# print(origrevs)
	allsent = getallsents('cameras.allsentences.txt')
	# print(allsent)
	revlist = getannotations('imscamb.annotationsonly.v1.txt', origrevs, allsent, 'labelled-sentences-kessler1.txt')
	savepickle(revlist, 'kesslerLabelled1.pickle')


if __name__ == '__main__':
	main()
