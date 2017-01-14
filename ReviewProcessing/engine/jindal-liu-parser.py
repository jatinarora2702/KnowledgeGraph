from nltk import tokenize
from nltk.tag import map_tag
from dataprocessing import store


import codecs
import nltk


gbcnt1 = 0
gbcnt2 = 0
gbcnt3 = 0
gbcnt4 = 0
data_dict = []
asplist = []
sent = []


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


def add(l, t):
    n = len(l)
    l[n - 1] += " " + t


def processLine(sent, line):
    global gbcnt1, gbcnt2, gbcnt3, gbcnt4, asplist
    l1 = []
    l2 = []
    l3 = []
    l4 = []
    tok = line.strip().split(" ")
    n = len(tok)
    f = 0
    i = 0
    for t in tok:
        if "1_" in t:
            l1.append(eatup(t))
            f = 1
        elif "2_" in t:
            l2.append(eatup(t))
            f = 2
        elif "3_" in t:
            l3.append(eatup(t))
            f = 3
        elif "(" in t or ")" in t and i >= n - 2:
            l4.append(eatup1(t))

            f = 4
        else:
            if f == 1:
                add(l1, t)
            elif f == 2:
                add(l2, t)
            elif f == 3:
                add(l3, t)
            elif f == 4:
                add(l4, t)
        i += 1
    gbcnt1 += len(l1)
    gbcnt2 += len(l2)
    gbcnt3 += len(l3)
    gbcnt4 += len(l4)
    asplist = l3
    print("Entity 1 : " + str(l1))
    print("Entity 2 : " + str(l2))
    print("Aspects  : " + str(l3))
    print("Opinions : " + str(l4))
    good = 1
    for item in l3:
        if item not in sent:
            good = 0
            break
    if good == 0:
        print("ERROR")
    print("----------------------\n")


def getPOS(sent):
    toksent = nltk.word_tokenize(sent)
    pos = nltk.pos_tag(toksent)
    return pos


def getPOSmod(sent):
    toksent = nltk.word_tokenize(sent)
    pos = nltk.pos_tag(toksent)
    simpTags = [(word, map_tag('en-ptb', 'universal', tag)) for word, tag in pos]
    return simpTags


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


def parseInp(filename):
    fr = codecs.open(filename, "r", 'cp1252')
    f = 0
    act = 0
    k = 0
    for line in fr:
        if act == 1:
            if f == c + 2:
                sent.append(line)
                k += 1
            elif f <= c:
                print("----------------------")
                # print (cleanSent(sent[k-1]))
                print("Sentence : " + sent[k - 1][:-1])
                print("\nPOS Tags : " + str(getPOS(cleanSent(sent[k - 1]))) + "\n")
                print("\nNew POS Tags : " + str(getPOSmod(cleanSent(sent[k - 1]))) + "\n")

                processLine(sent[k - 1], line)
                data_dict.append((k - 1, asplist))
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

    print(gbcnt1)
    print(gbcnt2)
    print(gbcnt3)
    print(gbcnt4)


def main():
	parseInp(store.DATA_PATH + "Jindal-Liu/labeledSentences.txt")
	# print (sent)
	# print (data_dict)


if __name__ == '__main__':
	main()
