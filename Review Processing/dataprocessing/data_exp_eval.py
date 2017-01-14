import codecs
import random
import re
from os import listdir
from os.path import isfile, join


DATA_PATH = '../../Data/final/'
OUT_DATA_PATH = '../../Data/final/results/'
STEP = 5
THRESHOLD = 30


def get_random_sents(totcnt, sentcnt):
	vec = random.sample(range(totcnt), sentcnt)
	return vec


def print_entries(line):
	print(line)
	# vec = line.split(' ')
	# for elem in vec:
	# 	if re.match(r'.*=.*', elem) is not None:
	# 		print(elem)


def getscore(predfile, sent):
	f = codecs.open(DATA_PATH + predfile, 'r', 'utf-8')
	fw = codecs.open(OUT_DATA_PATH + predfile, 'w', 'utf-8')
	pscore = 0
	ascore = 0
	e1score = 0
	e2score = 0
	print("seed: ", sent)
	cnt = 0
	for line in f:
		if line[0] == '{':
			print_entries(line)
			pscore += int(input('pred score = '))
			ascore += int(input('aspect score = '))
			e1score += int(input('e1 score = '))
			e2score += int(input('e2 score = '))
			cnt += 1
			if cnt % STEP == 0:
				print("cnt = ", cnt, " | tot pred score = ", pscore)
				print("cnt = ", cnt, " | tot aspect score = ", ascore)
				print("cnt = ", cnt, " | tot entity1 score = ", e1score)
				print("cnt = ", cnt, " | tot entity2 score = ", e2score)
				print("cnt = ", cnt, " | tot pred score = ", pscore, file=fw)
				print("cnt = ", cnt, " | tot aspect score = ", ascore, file=fw)
				print("cnt = ", cnt, " | tot entity1 score = ", e1score, file=fw)
				print("cnt = ", cnt, " | tot entity2 score = ", e2score, file=fw)
			if cnt >= THRESHOLD:
				break
		else:
			print('sent: ', line.split('/ ')[2])
	print("total predicate score = ", pscore)
	print("total aspect score = ", ascore)
	print("total entity1 score = ", e1score)
	print("total entity2 score = ", e2score)
	return pscore, ascore, e1score, e2score


def getfilelist(predfolder):
	path = DATA_PATH + predfolder
	filelist = [f for f in listdir(path) if isfile(join(path, f))]
	return filelist


def evaluate(sentlist, predfolder, sentnum):
	flist = getfilelist(predfolder)
	pat = 'expansion.' + str(sentnum) + '_'
	cnt = 0
	ptot = 0.0
	atot = 0.0
	e1tot = 0.0
	e2tot = 0.0
	for predfile in flist:
		if pat in predfile and '.scores' in predfile:
			print(predfile)
			cnt += 1
			ptemp, atemp, e1temp, e2temp = getscore(predfolder + '/' + predfile, sentlist[sentnum])
			ptot += ptemp
			atot += atemp
			e1tot += e1temp
			e2tot += e2temp
	if cnt > 0:
		ptot /= cnt
		atot /= cnt
		e1tot /= cnt
		e2tot /= cnt
	print("total sentence pred score = ", ptot)
	print("total sentence aspect score = ", atot)
	print("total sentence entity1 score = ", e1tot)
	print("total sentence entity2 score = ", e2tot)
	return ptot, atot, e1tot, e2tot


def calc(sentfile, predfolder, sentcnt, totsentcnt):
	f = codecs.open(DATA_PATH + sentfile, 'r', 'utf-8')
	sentlist = list()
	for sent in f:
		sentlist.append(sent)
	sentindex = get_random_sents(totsentcnt, sentcnt)
	ptot = 0.0
	atot = 0.0
	e1tot = 0.0
	e2tot = 0.0
	for sentnum in sentindex:
		ptemp, atemp, e1temp, e2temp = evaluate(sentlist, predfolder, sentnum)
		ptot = ptemp
		atot += atemp
		e1tot += e1temp
		e2tot += e2temp
	ptot /= sentcnt
	atot /= sentcnt
	e1tot /= sentcnt
	e2tot /= sentcnt
	return ptot, atot, e1tot, e2tot


def main():
	pscore, ascore, e1score, e2score = calc('labelled-sentences.txt', 'output-tempA', 2, 40)
	print("final pred score = ", pscore)
	print("final aspect score = ", ascore)
	print("final entity1 score = ", e1score)
	print("final entity2 score = ", e2score)


if __name__ == '__main__':
	main()
