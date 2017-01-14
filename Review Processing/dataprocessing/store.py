import codecs
import pickle
import re


# DATA_PATH = '../../Data/Reviews/Amazon/Electronics/'
# DATA_PATH = '../../Data/final/final-corrected/'
# DATA_PATH = '../../Data/Reviews/Jindal-Liu/'
DATA_PATH = '../../Data/all/'
PK_DATA_PATH = '../../Data/Reviews/Amazon/Electronics/'

def get_reviews_list(filename):
    filename = PK_DATA_PATH + filename
    review = dict()
    rev_list = list()
    with codecs.open(filename, 'r', encoding='utf-8') as f:
        for line in f:
            if line == '\n':
                rev_list.append(review)
                review = dict()
            else:
                item = line.split(": ")
                if len(item) == 2:
                    review[item[0]] = item[1]
    return rev_list


def store_as_pickle(filename, picklefile):
    rev_list = get_reviews_list(filename)
    picklefile = DATA_PATH + picklefile
    with open(picklefile, 'wb') as handle:
        pickle.dump(rev_list, handle)


def get_reviews_list_pickle(picklefile):
    picklefile = DATA_PATH + picklefile
    with open(picklefile, 'rb') as handle:
        rev_list = pickle.load(handle)
    return rev_list


def get_review_sentences(picklefile):
    rev_list = get_reviews_list_pickle(picklefile)
    print("Read Pickle File..")
    sent_list = list()
    for rev in rev_list:
        if 'review/text' in rev:
            sent_list.append(rev['review/text'])
    return sent_list


def make_review_sentences_file(sent_list, filename):
    filename = DATA_PATH + filename
    f = codecs.open(filename, 'w', 'utf-8')
    for sent in sent_list:
        f.write(sent)


def create_files_for_manual_labelling(picklefile, filename1, filename2):
    rev_list = get_reviews_list_pickle(picklefile)
    filename1 = DATA_PATH + filename1
    filename2 = DATA_PATH + filename2
    f1 = codecs.open(filename1, 'w', 'utf-8')
    f2 = codecs.open(filename2, 'w', 'utf-8')
    cnt = 0
    for rev in rev_list:
        if cnt < 10000:
            for key in rev:
                f1.write(key + ": " + rev[key])
            f1.write('\n\n')
        elif cnt < 20000:
            for key in rev:
                f2.write(key + ": " + rev[key])
            f2.write('\n\n')
        else:
            break
        cnt += 1


def write_review_to_file(filename, sentence_filename, picklefile, lrev):
    filename = DATA_PATH + filename
    f = codecs.open(filename, 'a', 'utf-8')
    for key in lrev:
        if key != 'review/labels':
            f.write(key + ": " + lrev[key])
    f.write('review/labels:\n')
    for sent in lrev['review/labels']:
        f.write('sentence: ' + sent['sentence'] + '\n')
        f.write('details:\n')
        for sentdict in sent['details']:
            for sentkey in sentdict:
                f.write(sentkey + ": " + str(sentdict[sentkey]) + '\n')
            f.write('\n')
    f.write("-------------------------")

    picklefile = DATA_PATH + picklefile
    try:
        with open(picklefile, 'rb') as handle:
            labelled_rev_list = pickle.load(handle)
    except:
        labelled_rev_list = list()
    labelled_rev_list.append(lrev)
    with open(picklefile, 'wb') as handle:
        pickle.dump(labelled_rev_list, handle)

    sentence_filename = DATA_PATH + sentence_filename
    fs = codecs.open(sentence_filename, 'a', 'utf-8')
    for sent in lrev['review/labels']:
        fs.write(sent['sentence'] + '\n')


def get_labelled_sentences(rev_list):
    sent_list = list()
    for rev in rev_list:
        sent_list = sent_list + rev['review/labels']
    return sent_list


def has_comparison(sent):
    allrelList = ["similar", "better", "more", "faster", "superior", "inferior", "bigger", "smaller", "improved", "larger", "higher", "less", "wider", "greater", "finer", "quicker", "improvement", "same", "identical", "taller", "step up", "nicer", "longer"]
    adjlist = ["bigger", "heavier", "cheaper", "upgraded", "predecessor", "upgrade", "lighter", "larger", "noiser", "faster", "smaller"]
    for elem in allrelList:
        if elem in sent:
            return True
    for elem in adjlist:
        if elem in sent:
            return True

    toklist = word_tokenize(sent)
    poslist = pos_tag(toklist)
    for entry in poslist:
        if entry[0] in allrelList or entry[0] in adjlist:
            return True
        if entry[1] == 'RBR' or entry[1] == 'JJR': # or entry[1] == 'JJ' or entry[1] == 'RB':
            return True
    return False


def get_sentences(rev, filename, sentlist):
    if 'review/text' in rev:
        vec = re.split(r'(?<!\w\.\w.)(?<![A-Z][a-z]\.)(?<=\.|\?|\!)\s', rev['review/text'])
        for sent in vec:
            if has_comparison(sent):
                sentlist.append(sent)
    return sentlist


def dumplist(filename, sentlist):
    f = codecs.open(filename, 'a', 'utf-8')
    for sent in sentlist:
        f.write(sent + '\n')


def create_unlabelled_sentences(picklefile):
    rev_list = get_reviews_list_pickle(picklefile)
    cnt = 0
    fcnt = 0
    sentlist = list()
    for rev in rev_list:
        if cnt % 1000 == 0:
            print(cnt)
        if (cnt > 2000 and cnt < 10000) or cnt > 12000:
            sentlist = get_sentences(rev, sentlist)
            if len(sentlist) >= 20000:
                fcnt += 1
                dumplist(DATA_PATH + 'Unlabelled/' + str(fcnt) + '.txt', sentlist)
                print('dumped in ', DATA_PATH + 'Unlabelled/' + str(fcnt) + '.txt')
                sentlist = list()
        cnt += 1
    fcnt += 1
    dumplist(DATA_PATH + 'Unlabelled/' + str(fcnt) + '.txt', sentlist)


def main():
    store_as_pickle('Amazon/Automotive.txt', 'Amazon/AutomotivePk.pickle')
    review_list = get_reviews_list_pickle('Amazon/AutomotivePk.pickle')
    sent_list = get_review_sentences('Electronics.txt, ElectronicsPk.pickle')
    store_as_pickle('Electronics.txt', 'ElectronicsPk.pickle')
    vec = get_review_sentences('ElectronicsPk.pickle')
    make_review_sentences_file(vec, 'Electronics_Reviews.txt')
    create_files_for_manual_labelling('ElectronicsPk.pickle', 'manual_labelling1.txt', 'manual_labelling2.txt')
    create_unlabelled_sentences('ElectronicsPk.pickle')
    revlist = get_reviews_list_pickle('labelledAll.pickle')
    for rev in revlist:
        for sent in rev['review/labels']:
            print(sent['sentence'])

if __name__ == '__main__':
    main()
