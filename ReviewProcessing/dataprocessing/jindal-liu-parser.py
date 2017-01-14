import codecs
import store
import pickle


def eatup(word):
    n = len(word)
    if n >= 3 and word[0] >= '0' and word[0] <= '9' and word[1] == "_":
        word = word[2:]
    return word


def eatup1(word):
    n = len(word)
    if n >= 2 and word[0] == '(':
        word = word[1:]
    if word[-1] == ')':
        word = word[:-1]
    return word


def findpos(toklist, tok):
    cnt = 0
    for elem in toklist:
        if tok in elem:
            return cnt
        cnt += 1
    return -1


def processLine(sent, line, toklist, revlist, cnt):
    l1 = []
    l2 = []
    l3 = []
    l4 = []
    tok = line.strip().split(" ")
    n = len(tok)
    i = 0
    for t in tok:
        if "1_" in t:
            l1.append(findpos(toklist, eatup(t)))
        elif "2_" in t:
            l2.append(findpos(toklist, eatup(t)))
        elif "3_" in t:
            l3.append(findpos(toklist, eatup(t)))
        elif "(" in t or ")" in t and i >= n - 2:
            l4.append(findpos(toklist, eatup1(t)))
        i += 1
    # print("Entity 1 : " + str(l1))
    # print("Entity 2 : " + str(l2))
    # print("Aspects  : " + str(l3))
    # print("Opinions : " + str(l4))
    preddict = dict()
    if(len(l4) > 0):
        preddict['predicate'] = l4[0]
        preddict['type'] = 1
        preddict['prod1'] = l1
        preddict['prod2'] = l2
        preddict['aspect'] = l3
        predlist = list()
        predlist.append(preddict)
        sentdict = dict()
        sentdict['sentence'] = sent
        sentdict['details'] = predlist
        sentlist = list()
        sentlist.append(sentdict)
        rev = dict()
        rev['review/text'] = sent
        rev['review/labels'] = sentlist
        revlist.append(rev)
    else:
    	print(cnt)
    # print("----------------------\n")
    return revlist


def cleanSent(sent):
    newsen = sent.replace(".", " ")
    newsen = newsen.replace(",", " ")
    newsen = newsen.replace("\n", " ")
    newsen = newsen.replace("(", " ")
    newsen = newsen.replace(")", " ")
    newsen = newsen.replace("`", " ")
    newsen = newsen.replace(" -", "-")
    newsen = newsen.replace("- ", "-")
    newsen = newsen.replace("--", " ")
    newsen = newsen.replace("!", " ")
    newsen = newsen.replace(":", " ")
    newsen = newsen.replace(";", " ")
    newsen = newsen.replace("$ ", "$")
    newsen = newsen.replace("%", "")
    newsen = newsen.replace("@", "")
    # newsen = newsen.replace("\' ", " ")
    # newsen = newsen.replace(" \'", "\'")
    newsen = newsen.replace("\'", " ")
    # newsen = newsen.replace("\'\'", " ")
    newsen = newsen.replace("\"", " ")
    # newsen = newsen.replace(" '", "'")
    newsen = newsen.strip()
    return newsen


def getfrompickle(picklefile):
    picklefile = store.DATA_PATH + picklefile
    with open(picklefile, 'rb') as handle:
        toklist = pickle.load(handle)
    return toklist


def saveaspickle(revlist, picklefile):
    picklefile = store.DATA_PATH + picklefile
    with open(picklefile, 'wb') as handle:
        pickle.dump(revlist, handle)


def parseInp(filename, tokfilename, picklefile):
    filename = store.DATA_PATH + filename
    fr = codecs.open(filename, "r", 'cp1252')
    vec = getfrompickle(tokfilename)
    sent = []
    f = 0
    act = 0
    k = 0
    revlist = list()
    cnt = 0
    for line in fr:
        if act == 1:
            if f == c + 2:
                sent.append(line)
                k += 1
            elif f <= c:
                # print("Sentence: ", sent[k-1][:-1])
                # print("k=", k)
                # print("Vector: ", vec[k-1])
                cnt += 1
                revlist = processLine(sent[k-1], line, vec[k-1], revlist, cnt)
            f -= 1
            if f == 0:
                act = 0
        elif "cs-1" in line or "cs-2" in line or "cs-3" in line:
            c1 = line.count("cs-1")
            c2 = line.count("cs-2")
            c3 = line.count("cs-3")
            c = c1 + c2 + c3
            f = c + 2
            act = 1

    print(len(vec))
    saveaspickle(revlist, picklefile)

def main():
    parseInp("labeledSentences.txt", "toksentsPk.pickle", "labelled-jindal.pickle")


if __name__ == '__main__':
    main()
