from nltk import tokenize
from bs4 import BeautifulSoup
from dataprocessing import store


import pymysql
import traceback
# import html2text
import re
import math
import nltk
# nltk.download()


compAdjs = []
with open(store.DATA_PATH + "Amazon/Camera/allComparableAdjectives.txt", "r") as rfile:
    for lines in rfile:
        if lines not in compAdjs:
            compAdjs.append(lines)


def checkIfinOpList(opinion):
    for item in compAdjs:
        if ((opinion in item) or (item in opinion)):
            return True
    return False


def checkIfValidOpinion(word, opinion):
    if "JJR" in word or "JJ" in word or "RBR" in word:
        if checkIfinOpList(opinion):
            return True
        else:
            return False
    else:
        return False


def checkIfOpinionIsVerb(word):
    word2 = word.split()
    newWord = word2[0]
    if ("VBZ" in newWord or "VBP" in newWord):
        return True
    else:
        return False


def checkIfOpinionForPat1(word):
    if word == "JJ" or word == "JJR" or word == "RBR" or word == "RB" or word == "VBN" or word == "IN" or word == "DT":
        return True
    else:
        return False


def checkIfOpinionForPat3and4(word):
    if word == "JJ" or word == "JJR" or word == "RBR" or word == "RB" or word == "VBN" or word == "IN" or word == "DT":  # or word == "DT" :
        return True
    else:
        return False


def getEarlierWords(sen, key):
    num = sen.find(key)

    newsen = sen[:num].strip()
    newsenarr = newsen.split()
    newsenarr.reverse()
    return newsenarr


def getNextWords(sen, key):
    num = sen.find(key)
    num = num + len(key)
    newsen = sen[num:].strip()
    newsenarr = newsen.split()
    if len(newsenarr) > 4:
        newsenarr = newsenarr[:4]
    return newsenarr


def getSurroundingWords(sen, key, num):
    surr = ""
    arr = sen.split()
    i = 0
    j = 0
    for i in range(len(arr)):
        if arr[i] == key:
            for j in range(1, num + 1):
                if (i + j) < len(arr):
                    if surr == "":
                        surr += arr[i + j]
                    else:
                        surr += " "
                        surr += arr[i + j]

            break

    return surr


###################1
def getAspectOpinionPairsPat1(sen, chkstr, foundasps, Algotype):
    pairs = {}  # pairs is a dict
    text = nltk.word_tokenize(chkstr)
    posmap = nltk.pos_tag(text)
    posmapdict = {}  # a dict
    for item in posmap:
        posmapdict[item[0]] = item[1]  # map dict ready
    chkarr = chkstr.split()
    # print foundasps
    for asp in foundasps.keys():

        if foundasps[asp] == "NF":  # do only if not done
            # print asp
            opinion = ""
            arr = asp.split()
            posstr = ""
            if ((len(arr) > 1 and asp in chkstr) or (
                    len(arr) == 1 and asp in chkstr)):  # means it is an aspect with more than one word
                if asp in chkstr:
                    wlist = getEarlierWords(chkstr, asp)  # got ewords before asp in sen in rev order
                    cnt = 0

                    for item in wlist:
                        if item not in posmapdict.keys():
                            if cnt < 1:
                                cnt += 1
                                continue
                            else:
                                break
                        else:
                            cnt = 0

                        pos = posmapdict[item]
                        posstr = pos + " " + posstr

                        newtext = nltk.word_tokenize(item)
                        posmap = nltk.pos_tag(newtext)
                        pos2 = posmap[0][1]

                        posstr = pos2 + " " + posstr

                        if checkIfOpinionForPat1(pos) or checkIfOpinionForPat1(pos2):
                            opinion = item + " " + opinion
                        # print opinion
                        else:
                            break

            if opinion != "" and checkIfValidOpinion(posstr, opinion):
                pairs[asp] = opinion
                if Algotype == 1:
                    foundasps[asp] = "F"

    return pairs


#########################################1 over



def checkdup(l, entry):
    s = set(l)
    origlen = len(s)
    s.add(entry)
    finallen = len(s)
    if origlen == finallen:
        return 0
    else:
        return 1


def getAspectOpinionPairsPat2(sen, chkstr, foundasps, Algotype):
    pairs = {}  # pairs is a dict
    text = nltk.word_tokenize(chkstr)
    posmap = nltk.pos_tag(text)
    posmapdict = {}  # a dict
    for item in posmap:
        posmapdict[item[0]] = item[1]  # map dict ready
    chkarr = chkstr.split()
    for asp in foundasps.keys():

        if foundasps[asp] == "NF":  # do only if not done
            opinion = ""
            fcntr = 0
            arr = asp.split()
            posstr = ""
            if ((len(arr) > 1 and asp in chkstr) or (
                    len(arr) == 1 and asp in chkstr)):  # means it is an aspect with more than one word

                wlist = getNextWords(chkstr, asp)  # got ewords before asp in sen in rev order
                cnt = 0
                # pat2
                for item in wlist:
                    if item not in posmapdict.keys():
                        if cnt < 1:
                            cnt += 1
                            continue
                        else:
                            break
                    else:
                        cnt = 0
                    newtext = nltk.word_tokenize(item)
                    posmap = nltk.pos_tag(newtext)
                    pos = posmap[0][1]

                    posstr = posstr + " " + pos

                    if fcntr == 0:
                        if checkIfOpinionIsVerb(pos):

                            fcntr += 1
                            continue
                        else:
                            break

                    op = opinion + " " + item
                    if checkIfOpinionForPat1(pos):
                        opinion = op

            # pat2
            if opinion != "" and checkIfValidOpinion(posstr, opinion):
                pairs[asp] = opinion
                if Algotype == 1:
                    foundasps[asp] = "F"

    return pairs


################################################################pat3
def getAspectOpinionPairsPat3(asplist, chkstr, foundasps):
    pairs = {}  # pairs is a dict
    text = nltk.word_tokenize(chkstr)
    posmap = nltk.pos_tag(text)
    posmapdict = {}  # a dict
    for item in posmap:
        posmapdict[item[0]] = item[1]  # map dict ready
    chkarr = chkstr.split()
    chkarr = chkarr[:3]
    for asp in asplist.keys():

        if foundasps[asp] == "NF":  # do only if not done
            opinion = ""
            fcntr = 0
            posstr = ""

            cnt = 0
            # pat3
            for item in chkarr:
                if item not in posmapdict.keys():
                    if cnt < 1:
                        cnt += 1
                        continue
                    else:
                        break

                else:
                    cnt = 0
                # PAT 3
                pos = posmapdict[item]

                if fcntr == 0:
                    if checkIfOpinionIsVerb(pos):
                        fcntr += 1
                        continue

                posstr = posstr + pos
                op = opinion + " " + item
                if checkIfOpinionForPat3and4(pos):
                    opinion = op

            # pat3

            if opinion != "" and checkIfValidOpinion(posstr, opinion):
                pairs[asp] = opinion
                foundasps[asp] = "F"

    return pairs


#####################################for 5 and 6
def getAspectOpinionPairsPat5and6(asplist, chkstr, foundasps):
    pairs = {}  # pairs is a dict
    text = nltk.word_tokenize(chkstr)
    posmap = nltk.pos_tag(text)
    posmapdict = {}  # a dict
    for item in posmap:
        posmapdict[item[0]] = item[1]  # map dict ready
    chkarr = chkstr.split()
    chkarr.reverse()
    chkarr = chkarr[:4]
    for asp in asplist.keys():

        if foundasps[asp] == "NF":  # do only if not done
            opinion = ""
            fcntr = 0
            posstr = ""

            cnt = 0
            # pat5
            for item in chkarr:
                if item not in posmapdict.keys():
                    if cnt < 1:
                        cnt += 1
                        continue
                    else:
                        break

                else:
                    cnt = 0
                # PAT 5
                pos = posmapdict[item]
                posstr = pos + " " + posstr
                op = item + " " + opinion
                if checkIfOpinionForPat3and4(pos):
                    opinion = op

            # pat5
            if opinion != "" and checkIfValidOpinion(posstr, opinion):
                pairs[asp] = opinion
                foundasps[asp] = "F"

    return pairs


db = pymysql.connect("localhost", "root", "sumana", "BTP")
cursor = db.cursor()

validrevids = []  # getting the training ids
with open(store.DATA_PATH + "Amazon/Camera/trainingIds.txt", "r") as f:
    lines = f.read().splitlines()
    for line in lines:
        validrevids.append(int(line.strip()))
f.close()

cats = []
aspectcat = {}
aspects = []

with open(store.DATA_PATH + "Amazon/Camera/aspects.txt", "r") as f:  # getting aspect mapping
    lines = f.read().splitlines()
    for line in lines:
        asp = line.split("->")[0].strip()  # map aspects
        cat = line.split("->")[1].strip()
        aspects.append(asp)
        cats.append(cat)
        aspectcat[asp] = cat
f.close()

aspects = set(aspects)

camMaping = {}

with open(store.DATA_PATH + "Amazon/Camera/camlist2.txt", "r") as f:  # getting camers
    lines = f.read().splitlines()
for camDetail2 in lines:
    if "with" in camDetail2:
        camdetailarr = camDetail2.split("with")
        camDetail = camdetailarr[0].strip()
    else:
        camDetail = camDetail2.strip()
    if camDetail != "" and "->" in camDetail:
        try:
            camcode = camDetail.split("->")[0].strip()
            camcode = camcode.replace("-", "")
            if "-" in camcode:
                print(camcode)
            if camcode in camMaping.keys():
                pass
            else:
                camMaping[camcode] = camDetail.split("->")[1].strip()
        except:
            traceback.print_exc()
            continue;

f.close()

allrelList = ["similar", "better", "more", "faster", "superior", "inferior", "bigger", "smaller", "improved", "larger",
              " higher", "less", "wider", "greater", "finer", "quicker", "improvement", "same", "identical", "taller",
              "step up", "nicer", "longer"]
adjlist = ["bigger", "heavier", "cheaper", "upgraded", "predecessor", "upgrade", "lighter", "larger", "noiser",
           "faster", "smaller"]
adjMapping = {}
adjMapping['bigger'] = "size" + "\t" + "bigger"
adjMapping['heavier'] = "weight" + "\t" + "heavier"
adjMapping['cheaper'] = "price" + "\t" + "cheaper"
adjMapping['upgraded'] = "version" + "\t" + "upgraded"
adjMapping['predecessor'] = "version" + "\t" + "predecessor"
adjMapping['upgrade'] = "version" + "\t" + "upgraded"
adjMapping['lighter'] = "weight" + "\t" + "lighter"
adjMapping['noiser'] = "noise performance" + "\t" + "worse"
adjMapping['faster'] = "focus speed" + "\t" + "faster"
adjMapping['smaller'] = "size" + "\t" + "smaller"
adjMapping['larger'] = "size" + "\t" + "larger"

letsscore = 0
anotherscore = 0
cnt = 0
flag = 1

sql = "select distinct ReviewText as Review , Title as arg1 , reviewid as rid from comProdRev Natural Join comProdData  group by ReviewText ;"

res = 0
sencntr = 0
sentence_list = []
with open(store.DATA_PATH + "Amazon/Camera/automated_tagging_V1.txt", "w") as f:
    cursor.execute(sql)
    data2 = cursor.fetchall()
    cntr = 0
    for values in data2:

        Review = values[0]
        arg1 = values[1]
        rid = values[2]
        strid = str(rid)
        if rid in validrevids:  # enter only if training id
            cntr += 1
            '''if cntr%50 == 0:
                print cnt'''

            try:
                rid = str(rid)
                # Review =  html2text.html2text(Review)
                soup = BeautifulSoup(Review, "lxml")
                Review = soup.get_text()
                arg1 = arg1.replace("-", "")
                prodarr = arg1.split()
                rev = re.split(r'(?<!\w\.\w.)(?<![A-Z][a-z]\.)(?<=\.|\?|\!)\s', Review)
                model1 = ""
                for wd in prodarr:
                    if wd in camMaping.keys():  # setting model1
                        model1 = wd

                if model1 == "":  # if model1 not set , leave
                    continue

                for revsen in rev:
                    sencntr += 1
                    anotherscore += 1
                    isComparedToDone = 0
                    revsen = revsen.replace('\n', ' ')
                    revsen = revsen.replace(',', ' ')
                    revsen = revsen.replace('/', ' ')
                    model1fg = 0
                    revarray = revsen.split();
                    # revarray = [word.replace(".", "").replace("," , "") for word in revarray]
                    '''print " start"
                    print revsen
                    print "end" '''
                    if len(revarray) > 30:  # its a very big sentence , dont want it
                        revsen = revsen[:250]
                    revarray = revsen.split();
                    revarray = [word.replace(".", "").replace(",", "") for word in revarray]

                    allAspectsDict = {}
                    for asp in aspects:  # get all aspects present in the sentence
                        revsenlower = revsen.lower()
                        if asp in revsen or asp in revsenlower:
                            allAspectsDict[asp] = "NF"
                    allImplicitadj = {}
                    for adj in adjlist:
                        if adj in revarray or adj in revsen.lower():
                            allImplicitadj[adj] = "NF"
                            # adjlower = adj.lower()

                    flag = 1
                    flag2 = 0
                    model2 = ""
                    for wd in revarray:  # check if model1 present in the sentence
                        if model1 == wd:
                            model1fg = 1
                            break

                    for word in revarray:  # check if sentense contains other camera

                        for cam in camMaping.keys():

                            foundasps = {}
                            foundadj = {}
                            foundrels = []
                            finalfound = []
                            finaladj = []
                            finalasp = []
                            flag = 1
                            flag2 = 0  # and not the same camera for which review was written
                            if word == cam:
                                flag = 0
                                model2 = cam

                            if flag == 0 and model1 != model2:  # not the same camera
                                origmodel2 = model2
                                origmodel1 = model1
                                prodarr.append(" ")
                                prodarr.append(" ")  # begin your pattern matching now
                                #########################################################################################################
                                # start pattern matching
                                finalsencntr = 0
                                letsscore += 1

                                arr = (revsen.lower()).split()

                                # regex1
                                # print "in 1"

                                patternRegex1 = "([^\n\r]*)\s+(than|as|to|over|compared|from|of|for) ((\w+\s*){0,3})" + model2

                                othercam = ""  # if any 3rd cam present
                                m = re.search(patternRegex1, revsen)
                                pattern = 1
                                if m:
                                    '''print "into 1"
                                    print revsen'''
                                    chkstr = m.groups()[0]
                                    newarr = chkstr.split()
                                    newarr = [word.replace(".", "").replace(",", "") for word in newarr]
                                    foundasps = {}
                                    foundadj = {}
                                    foundrels = []
                                    for cam in camMaping.keys():
                                        if cam in newarr and cam != model1 and cam != model2:
                                            othercam = cam  # other cam found
                                            break
                                    if (
                                                        model1 not in newarr and "it" not in newarr and "this" not in newarr and "its" not in newarr and "it\'s" not in newarr):
                                        if othercam != "":
                                            model1 = othercam  # set othercam

                                    pairs = getAspectOpinionPairsPat1(revsen, chkstr, allAspectsDict,
                                                                      1)  # get aspect-opinion pairs
                                    pairs2 = getAspectOpinionPairsPat2(revsen, chkstr, allAspectsDict,
                                                                       1)  # get aspect - opinion pairs from 2nd
                                    chkstr = chkstr.lower()

                                    for tuples in pairs.keys():
                                        if str(pairs[tuples]) in allImplicitadj:
                                            allImplicitadj[str(pairs[tuples])] = "F"
                                        foundrels.append(aspectcat[str(tuples)] + "\t" + str(pairs[tuples]))
                                    for tuples in pairs2.keys():
                                        if str(pairs2[tuples]) in allImplicitadj:
                                            allImplicitadj[str(pairs2[tuples])] = "F"
                                        foundrels.append(aspectcat[str(tuples)] + "\t" + str(pairs2[tuples]))

                                    for adj in adjlist:
                                        if (adj in chkstr.split() or adj in chkstr.lower().split()) and allImplicitadj[
                                            adj] == "NF":  # get additional adjs that come without asp
                                            allImplicitadj[adj] = "F"
                                            foundrels.append(adjMapping[str(adj)])

                                    earlierres = res
                                    for rel in foundrels:  # print them
                                        res += 1
                                        temp = str(revsen) + "\t" + str(model1) + "\t" + str(
                                            model2) + "\t" + rel + "\t" + str(pattern) + "\t" + rid
                                        if (checkdup(sentence_list, temp)):
                                            sentence_list.append(temp)
                                            print("1. " + temp)

                                        finalfound.append(str(model1) + "\t" + str(model2) + "\t" + rel.split("\t")[
                                            0] + "null" + "\t" + str(pattern))
                                        finalfound.append(
                                            str(model1) + "\t" + str(model2) + "\t" + "null\t" + rel.split("\t")[
                                                1] + "\t" + str(pattern))
                                        finalsencntr += 1

                                patternRegex2 = model2 + "([^\n\r]*)"
                                m = re.search(patternRegex2, revsen)
                                pattern = 2
                                model1 = origmodel1
                                model2 = origmodel2

                                if m:

                                    chkstr = m.groups()[0]
                                    newarr = chkstr.split()
                                    newarr = [word.replace(".", "").replace(",", "") for word in newarr]
                                    pattern = 2
                                    if "is" in newarr or "was" in newarr or "has" in newarr or "having" in newarr or "have" in newarr or "feel" in newarr:
                                        
                                        foundrels = []

                                        pairs = getAspectOpinionPairsPat1(revsen, chkstr, allAspectsDict,
                                                                          2)  # get aspect-opinion pairs
                                        for tuples in pairs.keys():
                                            if str(pairs[tuples]) in allImplicitadj:
                                                allImplicitadj[str(pairs[tuples])] = "F"
                                            foundrels.append(aspectcat[str(tuples)] + "\t" + str(pairs[tuples]))
                                            allAspectsDict[tuples] = "F"

                                        pairs2 = getAspectOpinionPairsPat2(revsen, chkstr, allAspectsDict, 2)
                                        for tuples in pairs2.keys():
                                            if str(pairs2[tuples]) in allImplicitadj:
                                                allImplicitadj[str(pairs2[tuples])] = "F"
                                            foundrels.append(aspectcat[str(tuples)] + "\t" + str(pairs2[tuples]))
                                            allAspectsDict[tuples] = "F"

                                        chkstr = chkstr.lower()
                                        for adj in adjlist:
                                            if (adj in chkstr.split()) and allImplicitadj[adj] == "NF":
                                                # print "into this"                 #get additional adjs that come without asp
                                                allImplicitadj[adj] = "F"
                                                foundrels.append(adjMapping[str(adj)])

                                        earlierres = res
                                        for rel in foundrels:  # print them
                                            res += 1
                                            # print 1
                                            temp = str(revsen) + "\t" + str(model2) + "\t" + str(
                                                model1) + "\t" + rel + "\t" + str(pattern) + "\t" + rid
                                            if (checkdup(sentence_list, temp)):
                                                sentence_list.append(temp)
                                                print("2. " + temp)

                                            finalfound.append(str(model2) + "\t" + str(model1) + "\t" + rel.split("\t")[
                                                0] + "null" + "\t" + str(pattern))
                                            finalfound.append(
                                                str(model2) + "\t" + str(model1) + "\t" + "null\t" + rel.split("\t")[
                                                    1] + "\t" + str(pattern))
                                            finalsencntr += 1

                                        if res == earlierres:
                                            for asp in foundasps:
                                                finalasp.append(str(model2) + "\t" + str(model1) + "\t" + str(
                                                    aspectcat[asp]) + "\t" + "null" + "\t" + str(pattern))
                                                finalsencntr += 1

                                # regex for this of that is better
                                var = "of"
                                patternRegex3 = "([^\n\r]*)\s+(of|in)\s+([^\n\r]*)" + model2 + "([^\n\r]*)"
                                pattern = 3
                                model1 = origmodel1
                                model2 = origmodel2
                                foundasps = {}
                                foundadj = {}
                                foundrels = []
                                m = re.search(patternRegex3, revsen)

                                othercam = ""
                                if m:
                                    var = m.groups()[1]
                                    posin = revsen.find(var)
                                    if math.fabs(revsen.find(model2) - posin) <= 10:

                                        chkstr = m.groups()[0]  # possible aspect
                                        chkstr = chkstr.lower()
                                        chkstr2 = m.groups()[3]  # possible adj
                                        if chkstr != "" and chkstr2 != "":

                                            newarr = chkstr2.split()
                                            newarr = [word.replace(".", "").replace(",", "") for word in newarr]
                                            foundasps = {}
                                            foundadj = {}
                                            foundrels = []
                                            for cam in camMaping.keys():
                                                if cam in newarr and cam != model1:
                                                    othercam = cam
                                                    break
                                            if (
                                                                model1 not in newarr and "it" not in newarr and "this" not in newarr and "its" not in newarr and "it\'s" not in newarr):
                                                if othercam != "":
                                                    model1 = othercam

                                            for asp in aspects:  # asp should be in chkstr and near "of / in "
                                                if asp in chkstr:
                                                    if (posin - (revsen.find(asp) + len(asp))) <= 5:
                                                        foundasps[asp] = chkstr.find(asp)

                                            pairs = getAspectOpinionPairsPat3(foundasps, chkstr2, allAspectsDict)
                                            for tuples in pairs.keys():
                                                foundrels.append(aspectcat[str(tuples)] + "\t" + str(pairs[tuples]))

                                            earlierres = res
                                            for rel in foundrels:  # print them
                                                res += 1
                                                # print 1
                                                temp = str(revsen) + "\t" + str(model2) + "\t" + str(
                                                    model1) + "\t" + rel + "\t" + str(pattern) + "\t" + rid
                                                if (checkdup(sentence_list, temp)):
                                                    sentence_list.append(temp)
                                                    print("3. " + temp)
                                                finalfound.append(
                                                    str(model1) + "\t" + str(model2) + "\t" + rel.split("\t")[
                                                        0] + "null" + "\t" + str(pattern))
                                                finalfound.append(str(model1) + "\t" + str(model2) + "\t" + "null\t" +
                                                                  rel.split("\t")[1] + "\t" + str(pattern))
                                                finalsencntr += 1

                                            if res == earlierres:
                                                for asp in foundasps:
                                                    finalasp.append(str(model1) + "\t" + str(model2) + "\t" + str(
                                                        aspectcat[asp]) + "\t" + "null" + "\t" + str(pattern))
                                                    finalsencntr += 1

                                # regex for this of that is better
                                var = "of"
                                patternRegex4 = "([^\n\r]*)\s+(of|in)\s+([^\n\r]*)(" + model1 + "|this)+([^\n\r]*)"
                                pattern = 4
                                model1 = origmodel1
                                model2 = ""
                                foundasps = {}
                                foundadj = {}
                                foundrels = []
                                m = re.search(patternRegex4, revsen)
                                othercam = ""
                                if m:
                                    var = m.groups()[1]
                                    var2 = m.groups()[3]
                                    posin = revsen.find(var)
                                    if math.fabs(revsen.find(var2) - posin) <= 10:

                                        chkstr = m.groups()[0]  # possible aspect
                                        chkstr = chkstr.lower()
                                        chkstr2 = m.groups()[4]  # possible adj

                                        if chkstr != "" and chkstr2 != "":

                                            newarr = chkstr2.split()
                                            newarr = [word.replace(".", "").replace(",", "") for word in newarr]
                                            for cam in camMaping.keys():
                                                if cam in newarr:
                                                    if cam != model1:
                                                        model2 = cam
                                                        break
                                            if model2 != "":
                                                foundasps = {}
                                                foundadj = {}
                                                foundrels = []

                                                for asp in aspects:  # asp should be in chkstr and near "of / in "
                                                    if asp in chkstr:
                                                        if (posin - (revsen.find(asp) + len(asp))) <= 5:
                                                            foundasps[asp] = chkstr.find(asp)

                                                pairs = getAspectOpinionPairsPat3(foundasps, chkstr2, allAspectsDict)
                                                for tuples in pairs.keys():
                                                    foundrels.append(aspectcat[str(tuples)] + "\t" + str(pairs[tuples]))

                                                earlierres = res
                                                for rel in foundrels:  # print them
                                                    res += 1
                                                    # print 1
                                                    temp = str(revsen) + "\t" + str(model1) + "\t" + str(
                                                        model2) + "\t" + rel + "\t" + str(pattern) + "\t" + rid
                                                    if (checkdup(sentence_list, temp)):
                                                        sentence_list.append(temp)
                                                        print("4. " + temp)
                                                    finalfound.append(
                                                        str(model1) + "\t" + str(model2) + "\t" + rel.split("\t")[
                                                            0] + "null" + "\t" + str(pattern))
                                                    finalfound.append(
                                                        str(model1) + "\t" + str(model2) + "\t" + "null\t" +
                                                        rel.split("\t")[1] + "\t" + str(pattern))
                                                    finalsencntr += 1

                                                if res == earlierres:
                                                    for asp in foundasps:
                                                        finalasp.append(str(model1) + "\t" + str(model2) + "\t" + str(
                                                            aspectcat[asp]) + "\t" + "null" + "\t" + str(pattern))
                                                        finalsencntr += 1

                                # next regex would be "model1  ADJ  than model2  PROPERTY"

                                patternRegex5 = "([^\n\r]*)\s+(than|as|to|over|compared|from|of)\s+((\w+\s*){0,3})" + model2 + "([^\n\r]*)"
                                m = re.search(patternRegex5, revsen)
                                pattern = 5
                                model1 = origmodel1
                                model2 = origmodel2
                                othercam = ""
                                if m:
                                    chkstr = m.groups()[0]
                                    chkstr2 = m.groups()[4]
                                    camchkstr = chkstr2
                                    compstr = m.groups()[1]
                                    poscomp = revsen.find(compstr)

                                    if chkstr != "" and chkstr2 != "":

                                        newarr = chkstr.split()
                                        newarr = [word.replace(".", "").replace(",", "") for word in newarr]
                                        foundasps = {}
                                        foundadj = {}
                                        foundrels = []

                                        for cam in camMaping.keys():
                                            if cam in newarr:
                                                if cam != model1 and cam != model2:
                                                    model1 = cam
                                                    break

                                        for asp in aspects:  # get positions of words
                                            if asp in chkstr2:
                                                foundasps[asp] = chkstr2.find(asp)

                                        pairs = getAspectOpinionPairsPat5and6(foundasps, chkstr, allAspectsDict)
                                        for tuples in pairs.keys():
                                            if (poscomp - revsen.find(pairs[tuples]) <= 10):
                                                foundrels.append(aspectcat[str(tuples)] + "\t" + str(pairs[tuples]))

                                        earlierres = res
                                        for rel in foundrels:  # print them
                                            res += 1
                                            # print 1
                                            temp = str(revsen) + "\t" + str(model1) + "\t" + str(
                                                model2) + "\t" + rel + "\t" + str(pattern) + "\t" + str(rid)
                                            if (checkdup(sentence_list, temp)):
                                                sentence_list.append(temp)
                                                print("5. " + temp)
                                            finalfound.append(str(model1) + "\t" + str(model2) + "\t" + rel.split("\t")[
                                                0] + "null" + "\t" + str(pattern))
                                            finalfound.append(
                                                str(model1) + "\t" + str(model2) + "\t" + "null\t" + rel.split("\t")[
                                                    1] + "\t" + str(pattern))
                                            finalsencntr += 1

                                        if res == earlierres:
                                            for asp in foundasps:
                                                finalasp.append(str(model1) + "\t" + str(model2) + "\t" + str(
                                                    aspectcat[asp]) + "\t" + "null" + "\t" + str(pattern))
                                                finalsencntr += 1

                                # next regex would be model2  ADJ  than model1  PROPERTY

                                patternRegex6 = "([^\n\r]*)\s+(than|as|to|over|compared|from|of)\s+((\w+\s*){0,3})(" + model1 + "|this)([^\n\r]*)"
                                m = re.search(patternRegex6, revsen)
                                pattern = 6
                                model1 = origmodel1
                                model2 = origmodel2
                                model2 = ""

                                if m:
                                    chkstr = m.groups()[0]  # before compared to
                                    compstr = m.groups()[1]
                                    poscomp = revsen.find(compstr)
                                    chkstr2 = m.groups()[5]  # after compared to
                                    camchkstr = chkstr2

                                    if chkstr != "" and chkstr2 != "":

                                        newarr = chkstr.split()
                                        newarr = [word.replace(".", "").replace(",", "") for word in newarr]
                                        foundasps = {}
                                        foundadj = {}
                                        foundrels = []

                                        for cam in camMaping.keys():
                                            if cam in newarr:
                                                if cam != model1:
                                                    model2 = cam
                                                    break

                                        if model2 != "":
                                            for asp in aspects:  # get positions of words
                                                if asp in chkstr2:
                                                    foundasps[asp] = chkstr2.find(asp)

                                            pairs = getAspectOpinionPairsPat5and6(foundasps, chkstr, allAspectsDict)
                                            for tuples in pairs.keys():
                                                if (poscomp - revsen.find(pairs[tuples]) <= 10):
                                                    foundrels.append(aspectcat[str(tuples)] + "\t" + str(pairs[tuples]))

                                            earlierres = res
                                            for rel in foundrels:  # print them
                                                res += 1
                                                # print 1

                                                temp = str(revsen) + "\t" + str(model2) + "\t" + str(
                                                    model1) + "\t" + rel + "\t" + str(pattern) + "\t" + str(rid)
                                                if (checkdup(sentence_list, temp)):
                                                    sentence_list.append(temp)
                                                    print("6. " + temp)
                                                finalfound.append(
                                                    str(model1) + "\t" + str(model2) + "\t" + rel.split("\t")[
                                                        0] + "null" + "\t" + str(pattern))
                                                finalfound.append(str(model1) + "\t" + str(model2) + "\t" + "null\t" +
                                                                  rel.split("\t")[1] + "\t" + str(pattern))
                                                finalsencntr += 1

                                            if res == earlierres:
                                                for asp in foundasps:
                                                    finalasp.append(str(model1) + "\t" + str(model2) + "\t" + str(
                                                        aspectcat[asp]) + "\t" + "null" + "\t" + str(pattern))
                                                    finalsencntr += 1

                                '''
                                for item in finalasp:
                                    if item not in finalfound:  #final found is a complete relationship
                                        finalsencntr+=1
                                        print str(revsen)+"\t"+str(item)

                                for item in finaladj:
                                    if item not in finalfound:
                                        print str(revsen)+"\t"+item

                                if finalsencntr ==0:
                                    print str(revsen)+"\t"+str(model1)+"\t"+str(origmodel2)+"\t"+"null"+"\t"+"null" '''

            except Exception as ex:
                print(ex)
                traceback.print_exc()


print(sencntr)
