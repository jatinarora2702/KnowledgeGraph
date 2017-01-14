import codecs

with codecs.open("features10_train1.csv", 'w') as fw:
    with codecs.open("features10_test1.csv", 'w') as fw1:
        ycnt = 0
        ncnt = 0
        f = codecs.open("features10.csv", 'r')
        for line in f:
            if "no," in line:
                ncnt += 1
            elif "yes," in line:
                ycnt += 1
        print("yes cnt: " + str(ycnt))
        print("no cnt: " + str(ncnt))
        ytrain = int(input("Enter ytrain cnt: "))
        ntrain = int(input("Enter ntrain cnt: "))
        tncnt = tycnt = 0
        f = codecs.open("features10.csv", 'r')
        temp = f.readline()
        fw.write(temp)
        fw1.write(temp)
        for line in f:
            if "no," in line:
                if tncnt < ntrain:
                    fw.write(line)
                else:
                    fw1.write(line)
                tncnt += 1
            elif "yes," in line:
                if tycnt < ytrain:
                    fw.write(line)
                else:
                    fw1.write(line)
                tycnt += 1
