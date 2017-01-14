from nltk import tokenize
from nltk.tag import map_tag


import codecs
import math
import nltk
import operator
import traceback


gbcnt1 = 0
gbcnt2 = 0
gbcnt3 = 0
gbcnt4 = 0


def isValidBgram(pos1, pos2):
    if "NOUN" in pos1 and "NOUN" in pos2:
        return True
    elif "ADJ" in pos1 and "NOUN" in pos2:
        return True
    else:
        return False


def getbiagrams(sen):
    retarr = []
    arr = sen.split()
    index = 0
    for index in range(0, len(arr) - 1):
        newbgram = arr[index] + " " + arr[index + 1]
        retarr.append(newbgram)
    return retarr


def isValid(pos):
    if "NOUN" in pos or "VERB" in pos:
        return True
    else:
        return False


def getFeatures1(toksent, dspos, findex, lindex):
    tot = len(toksent)
    feat = [None] * 8;
    if findex - 2 < 0:
        feat[0] = 'NONE'
        feat[6] = 'NONE'
    else:
        feat[0] = toksent[findex - 2]
        feat[6] = dspos[findex - 2][1]

    if findex - 1 < 0:
        feat[1] = 'NONE'
        feat[4] = 'NONE'
    else:
        feat[1] = toksent[findex - 1]
        feat[4] = dspos[findex - 1][1]

    if lindex + 1 >= tot:
        feat[2] = 'NONE'
        feat[5] = 'NONE'
    else:
        feat[2] = toksent[lindex + 1]
        feat[5] = dspos[lindex + 1][1]

    if lindex + 2 >= tot:
        feat[3] = 'NONE'
        feat[7] = 'NONE'
    else:
        feat[3] = toksent[lindex + 2]
        feat[7] = dspos[lindex + 2][1]
    return feat


def getFeaturesList(sent, tokenstring):
    # toksent = sent.split()
    toksent = nltk.word_tokenize(sent)
    dspos_old = nltk.pos_tag(toksent)
    dspos = [(word, map_tag('en-ptb', 'universal', tag)) for word, tag in dspos_old]
    tokenlist = tokenstring.split()
    n = len(toksent)
    m = len(tokenlist)
    featlist = []
    for i in range(n):
        f = 1
        for j in range(m):
            if toksent[i + j] != tokenlist[j]:
                f = 0
                break
        if f == 1:
            idfirst = i
            idlast = i + m - 1
            featlist.append(getFeatures1(toksent, dspos, idfirst, idlast))
    return featlist


def checkIfTokenPartOfAspect(token, aspectlist):
    for items in aspectlist:
        if token in items or token == items:
            return False
    return True


def checkIfAspect(token, aspectlist):
    for item in aspectlist:
        if token == item:
            return True
    return False


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
    global gbcnt1, gbcnt2, gbcnt3, gbcnt4
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
    # print("L1 : " + str(l1))
    # print("L2 : " + str(l2))
    # print("L3 : " + str(l3))
    # print("L4 : " + str(l4))
    good = 1
    for item in l3:
        if item not in sent:
            good = 0
            break
    '''if good == 0:
        print ("ERROR")
    print ("----------------------\n")'''
    return l3


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


def myprinter(d, dsent):
    sorted_d = sorted(d.items(), key=operator.itemgetter(1))
    for tup in reversed(sorted_d):
        print("========================================================================================")
        print(str(tup[0]) + ": " + str(tup[1]))
        if int(tup[1]) >= 3:  # threshold
            sentSet = list(set(dsent[tup[0]]))
            for item in sentSet:
                print("-{" + str(item) + "}-")
        print("========================================================================================")


def main():

    f = 0
    k = 0
    act = 0
    cntr = 0
    nocases = 0
    yescases = 0

    pattern_dict = {}
    pattern_dict_sent = {}
    sent = []
    asplist = []

    fr = codecs.open("labeledSentences.txt", "r", 'cp1252')
    for line in fr:
        if act == 1:
            if f == c + 2:
                sent.append(line)
                k += 1
            elif f <= c:
                # print ("----------------------")
                asplist = processLine(sent[k - 1], line)  # got aspects for that line
                newsen = cleanSent(sent[k - 1])
                # newsen is ready
                try:
                    wlist = newsen.split()  # wordlist is ready
                    biagramlist = getbiagrams(newsen)
                    text = nltk.word_tokenize(newsen)
                    posmap_old = nltk.pos_tag(text)
                    posmap = [(word, map_tag('en-ptb', 'universal', tag)) for word, tag in posmap_old]
                    posmapdict = {}  # a dict
                    for item in posmap:
                        posmapdict[item[0]] = item[1]
                    for token in wlist:  # done for unigrams
                        if token in posmapdict.keys():
                            posOftoken = posmapdict[token]
                            if isValid(posOftoken):  # begin your thing
                                if (checkIfTokenPartOfAspect(token, asplist)):
                                    flist = getFeaturesList(newsen, token)
                                    # print comma separated
                                    for features in flist:
                                        # print ("no,"+features[0]+","+features[1]+","+features[2]+","+features[3]+","+features[4]+","+features[5]+","+features[6]+","+features[7])
                                        nocases += 1

                                elif (checkIfAspect(token, asplist)):
                                    flist = getFeaturesList(newsen, token)
                                    # print comma separated
                                    for features in flist:
                                        ltemp = []
                                        ltemp.append(features[6])
                                        ltemp.append(features[4])
                                        ltemp.append(posOftoken)
                                        ltemp.append(features[5])
                                        ltemp.append(features[7])
                                        if str(ltemp) in pattern_dict.keys():
                                            pattern_dict[str(
                                                ltemp)] += 1  # uncomment this line and comment next to print only patterns
                                            pattern_dict_sent[str(ltemp)].append(newsen)
                                        else:
                                            pattern_dict[str(ltemp)] = 1  # uncomment this line and comment next lines to print only patterns
                                            sentlist = []
                                            sentlist.append(newsen)
                                            pattern_dict_sent[str(ltemp)] = sentlist
                                        # print ("yes,"+features[0]+","+features[1]+","+features[2]+","+features[3]+","+features[4]+","+features[5]+","+features[6]+","+features[7])
                                        yescases += 1

                    # now do for bigrams
                    for token in biagramlist:
                        tokarr = token.split()
                        elem1 = tokarr[0]
                        elem2 = tokarr[1]
                        if elem1 in posmapdict.keys() and elem2 in posmapdict.keys():
                            pos1 = posmapdict[elem1]
                            pos2 = posmapdict[elem2]
                        if isValidBgram(pos1, pos2):
                            if (checkIfAspect(token, asplist)):
                                flist = getFeaturesList(newsen, token)
                                # print comma separated
                                for features in flist:
                                    ltemp = []
                                    ltemp.append(features[6])
                                    ltemp.append(features[4])
                                    ltemp.append(pos1)
                                    ltemp.append(pos2)
                                    ltemp.append(features[5])
                                    ltemp.append(features[7])
                                    # print (ltemp)
                                    if str(ltemp) in pattern_dict.keys():
                                        # print ("COOL2")
                                        pattern_dict[
                                            str(ltemp)] += 1  # uncomment this line and comment next to print only patterns
                                        pattern_dict_sent[str(ltemp)].append(newsen)
                                    else:
                                        # print ("FIRST2")
                                        pattern_dict[str(
                                            ltemp)] = 1  # uncomment this line and comment next lines to print only patterns
                                        sentlist = []
                                        sentlist.append(newsen)
                                        pattern_dict_sent[str(ltemp)] = sentlist
                                    # print ("yes,"+features[0]+","+features[1]+","+features[2]+","+features[3]+","+features[4]+","+features[5]+","+features[6]+","+features[7])
                                    yescases += 1

                            else:
                                flist = getFeaturesList(newsen, token)
                                # print comma separated
                                for features in flist:
                                    # print ("no,"+features[0]+","+features[1]+","+features[2]+","+features[3]+","+features[4]+","+features[5]+","+features[6]+","+features[7])
                                    nocases += 1
                except:
                    # traceback.print_exc()
                    pass

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


    myprinter(pattern_dict, pattern_dict_sent)
    print("========================================================================================")
    print(yescases)
    print(nocases)


if __name__ == '__main__':
	main()
