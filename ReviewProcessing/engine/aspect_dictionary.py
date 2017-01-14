import operator
import re
import codecs
from nltk import pos_tag
from nltk.stem import PorterStemmer, WordNetLemmatizer
from nltk.tokenize import sent_tokenize, word_tokenize
from nltk.corpus import stopwords, wordnet
from dataprocessing import store

UTHRESHOLD = 1
BTHRESHOLD = 1


def sentence_segment_regex(para):
    return re.split(r'(?<!\w\.\w.)(?<![A-Z][a-z]\.)(?<=\.|\?|!)\s', para)


def sentence_segment_nltk(para):
    return sent_tokenize(para)


def is_not_stop_word(word):
    return word not in stopwords.words('english')


def has_numeral(word):
    return bool(re.search(r'\d', word))


def isvalid_unigram_aspect(term):
    valid = term[1] == 'NN' or term[1] == 'NNS'
    valid &= is_not_stop_word(term[0])
    # valid &= not has_numeral(term[0])
    return valid


def isvalid_bigram_aspect(prev, curr):
    valid = prev[1] == 'NN' or prev[1] == 'NNS' or prev[1] == 'JJ'
    valid &= curr[1] == 'NN' or curr[1] == 'NNS'
    valid &= is_not_stop_word(prev[0])
    valid &= is_not_stop_word(curr[0])
    # valid &= not has_numeral(prev[0])
    # valid &= not has_numeral(curr[0])
    return valid


def term_normalize_stem(word):
    word = word.lower()
    stemmer = PorterStemmer()
    word = stemmer.stem(word)
    return word


def term_normalize_lemmatize(word):
    word = word.lower()
    lemmatizer = WordNetLemmatizer()
    word = lemmatizer.lemmatize(word)
    return word


def update_unigrams(pos_list, aspect_dict):
    for term in pos_list:
        if isvalid_unigram_aspect(term):
            u = term_normalize_lemmatize(term[0])
            if u not in aspect_dict:
                aspect_dict[u] = 1
            else:
                aspect_dict[u] += 1


def update_bigrams(pos_list, aspect_dict):
    prev = ('$', '.')
    for term in pos_list:
        if isvalid_bigram_aspect(prev, term):
            b = term_normalize_lemmatize(prev[0]) + ' ' + term_normalize_lemmatize(term[0])
            if b not in aspect_dict:
                aspect_dict[b] = 1
            else:
                aspect_dict[b] += 1
        prev = term


def get_sentence_aspects(review, aspect_dict_unigram, aspect_dict_bigram):
    sent_list = sentence_segment_regex(review)
    for sent in sent_list:
        token_list = word_tokenize(sent)
        pos_list = pos_tag(token_list)
        update_unigrams(pos_list, aspect_dict_unigram)
        update_bigrams(pos_list, aspect_dict_bigram)


def get_sorted_list(aspect_dict):
    return sorted(aspect_dict.items(), key=operator.itemgetter(1), reverse=True)


def get_synonyms(word):
    synonyms = list()
    vec = list()
    for ss in wordnet.synsets(word):
        for w in ss.lemmas():
            vec.append(w.name())
    vec = list(set(vec))
    for w in vec:
        w = w.replace('_', ' ')
        synonyms.append(w)
    print(synonyms)
    return synonyms


def make_aspects_list(filename, picklefile):
    review_sent_list = store.get_review_sentences(picklefile)
    aspect_dict_unigram = dict()
    aspect_dict_bigram = dict()
    for review in review_sent_list[0:1]:
        get_sentence_aspects(review, aspect_dict_unigram, aspect_dict_bigram)

    aspects_list_unigram = get_sorted_list(aspect_dict_unigram)
    aspects_list_bigram = get_sorted_list(aspect_dict_bigram)
    # print(aspects_list_unigram)
    # print(aspects_list_bigram)
    asp_unigram_filename = store.DATA_PATH + filename + '-unigram.txt'
    asp_bigram_filename = store.DATA_PATH + filename + '-bigram.txt'
    asp_freq_unigram_filename = store.DATA_PATH + filename + '-unigram-freq.txt'
    asp_freq_bigram_filename = store.DATA_PATH + filename + '-bigram-freq.txt'
    f1 = codecs.open(asp_unigram_filename, 'w', encoding='utf-8')
    f2 = codecs.open(asp_bigram_filename, 'w', encoding='utf-8')
    f3 = codecs.open(asp_freq_unigram_filename, 'w', encoding='utf-8')
    f4 = codecs.open(asp_freq_bigram_filename, 'w', encoding='utf-8')
    for aspect in aspects_list_unigram:
        if aspect[1] >= UTHRESHOLD:
            f1.write(aspect[0] + '\n')
        f3.write(aspect[0] + " : " + str(aspect[1]) + '\n')
    for aspect in aspects_list_bigram[:BTHRESHOLD]:
        if aspect[1] >= BTHRESHOLD:
            f2.write(aspect[0] + '\n')
        f4.write(aspect[0] + " : " + str(aspect[1]) + '\n')


def get_aspects(dataset, manual, filename):
    if manual:
        filename = store.DATA_PATH + dataset + '/Aspects/manual/' + filename
    else:
        filename = store.DATA_PATH + dataset + '/Aspects/auto/' + filename
    aspect_list = list()
    with codecs.open(filename, 'r', encoding='utf-8') as f:
        for asp in f:
            aspect_list.append(asp[:-1])
    return aspect_list


def main():
    make_aspects_list('Amazon/Aspects/auto/Automotive', 'Amazon/AutomotivePk.pickle')
    get_synonyms('camera')
    asp_list = get_aspects('Amazon', False, 'Automotive-unigram.txt')

if __name__ == '__main__':
    main()
