from dataprocessing import store
import codecs


SIMILARITY_THRESHOLD = 2


def read_gold_data(filename, sent, e1, e2, asp, opinion):
    f = codecs.open(filename, 'r', encoding='utf-8')
    for line in f:
        vec = line.split('\t')
        sent.append(vec[0])
        e1.append(vec[1])
        e2.append(vec[2])
        asp.append(vec[3])
        opinion.append(vec[4])


def print_accuracy(accuracy):
    for key, value in accuracy.items():
        print(key + " : " + str(value))


def get_index(arr, sent):
    try:
        return arr.index(sent)
    except:
        return -1


def check(gold, test, index):
    if test in gold[index] or gold[index] in test:
        n = len(test.split(' '))
        m = len(gold[index].split(' '))
        if abs(n - m) <= SIMILARITY_THRESHOLD:
            return 1
        else:
            return 0
    else:
        return 0


def evaluate(test_filename, gold_filename):
    sent = list()
    e1 = list()
    e2 = list()
    asp = list()
    opinion = list()
    read_gold_data(gold_filename, sent, e1, e2, asp, opinion)
    correct = dict()
    correct['e1'] = 0
    correct['e2'] = 0
    correct['asp'] = 0
    correct['opinion'] = 0
    correct['total'] = 0
    f = codecs.open(test_filename, 'r', encoding='utf-8')
    for line in f:
        vec = line.split('\t')
        index = get_index(sent, vec[0])
        print(vec)
        print(e1[index])
        print(e2[index])
        print(asp[index])
        print(opinion[index])
        if index >= 0:
            v1 = check(e1, vec[1], index)
            v2 = check(e2, vec[2], index)
            v3 = check(asp, vec[3], index)
            v4 = check(opinion, vec[4], index)
            correct['e1'] += v1
            correct['e2'] += v2
            correct['asp'] += v3
            correct['opinion'] += v4
            if v1 == 1 and v2 == 1 and v3 == 1 and v4 == 1:
                correct['total'] += 1
    accuracy = dict()
    accuracy['e1'] = correct['e1'] / len(sent)
    accuracy['e2'] = correct['e2'] / len(sent)
    accuracy['asp'] = correct['asp'] / len(sent)
    accuracy['opinion'] = correct['opinion'] / len(sent)
    accuracy['total'] = correct['total'] / len(sent)
    return accuracy


def main():
    accuracy = evaluate(store.DATA_PATH + 'Amazon/Camera/test.txt', store.DATA_PATH + 'Amazon/Camera/gold.txt')
    print_accuracy(accuracy)


if __name__ == '__main__':
    main()
