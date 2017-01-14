import codecs
import pickle
import nltk
from nltk.tokenize import word_tokenize


DATA_PATH = '../../Data/final/final-corrected/'


def getrevs(picklefile):
	picklefile = DATA_PATH + picklefile
	with open(picklefile, 'rb') as handle:
		revlist = pickle.load(handle)
	return revlist


def savefile(picklefile, revlist):
	picklefile = DATA_PATH + picklefile
	with open(picklefile, 'wb') as handle:
		pickle.dump(revlist, handle)


def printsent(sent, toklist):
	cnt = 0
	for tok in toklist:
		cnt += 1
		print(cnt, ': ', tok)
	print()


def printdetails(sent, sentdict, toklist):
	n = len(toklist)
	print('pred: (', sentdict['predicate']+1, ') ', toklist[sentdict['predicate']] if sentdict['predicate'] >= 0 and sentdict['predicate'] < n else 'NONE')
	print('type: ', sentdict['type'])
	print('asp: (', sentdict['aspect']+1, ') ', toklist[sentdict['aspect']] if sentdict['aspect'] >= 0 and sentdict['aspect'] < n else 'NONE')
	print('prod1: (', sentdict['prod1']+1, ') ', toklist[sentdict['prod1']] if sentdict['prod1'] >= 0 and sentdict['prod1'] < n else 'NONE')
	print('prod2: (', sentdict['prod2']+1, ') ', toklist[sentdict['prod2']] if sentdict['prod2'] >= 0 and sentdict['prod2'] < n else 'NONE')


def read_toksents(picklefile):
	picklefile = DATA_PATH + picklefile
	with open(picklefile, 'rb') as handle:
		vec = pickle.load(handle)
	return vec


def runchecker(picklefile, toksentlist, num):
	revlist = getrevs(picklefile)
	cnt = 0
	rcnt = 0
	for rev in revlist:
		rcnt += 1
		scnt = 0
		for sent in rev['review/labels']:
			cnt += 1
			scnt += 1
			if cnt >= num:
				print('-------------------------------------------------------------------------------------')
				print('sentence ', cnt, ': ', sent['sentence'])
				printsent(sent['sentence'], toksentlist[cnt-1])
				pcnt = 0
				for sentdict in sent['details']:
					pcnt += 1
					printdetails(sent['sentence'], sentdict, toksentlist[cnt-1])
					correct = int(input('correct?: '))
					if correct == 0:
						consider = int(input('consider(1)/remove(0)?: '))
						if consider != 0:
							sentdict['predicate'] = int(input('predicate: ')) - 1
							sentdict['type'] = int(input('type: '))
							sentdict['aspect'] = int(input('aspect: ')) - 1
							sentdict['prod1'] = int(input('prod1: ')) - 1
							sentdict['prod2'] = int(input('prod2: ')) - 1
							savefile(picklefile, revlist)
						else:
							del revlist[rcnt-1]['review/labels'][scnt-1]['details'][pcnt-1]
							if len(revlist[rcnt-1]['review/labels'][scnt-1]['details']) == 0:
								del revlist[rcnt-1]['review/labels'][scnt-1]
								del toksentlist[cnt-1]
								if len(revlist[rcnt-1]['review/labels']) == 0:
									del revlist[rcnt-1]
							savefile(picklefile, revlist)
							savefile('toksentsPk.pickle', toksentlist)
							exit()


def main():
	toksentlist = read_toksents('toksentsPk.pickle')
	num = int(input('Start from sentence index: '))
	runchecker('labelled2Pk.pickle', toksentlist, num)


if __name__ == '__main__':
	main()
