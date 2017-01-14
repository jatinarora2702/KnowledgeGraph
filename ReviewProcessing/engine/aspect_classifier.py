from sklearn import metrics
from sklearn.linear_model import LogisticRegression
from sklearn.naive_bayes import BernoulliNB
from sklearn.naive_bayes import MultinomialNB
from sklearn.naive_bayes import GaussianNB
from sklearn.neural_network import BernoulliRBM
from sklearn.svm import SVC
from sklearn.tree import DecisionTreeClassifier
from sklearn.tree import ExtraTreeClassifier
from sklearn.feature_extraction import DictVectorizer as DV
from sklearn.cross_validation import train_test_split
from dataprocessing import store


import numpy as np
import pandas as pd


def main():
	input_file = store.DATA_PATH + "Jindal-Liu/Training/aspect_features.csv"
	data = pd.read_csv(input_file, header=0)


	train_data, test_data = train_test_split(data, train_size=0.6)

	# print("TRAIN DATA")
	# print(train_data)

	X_train = train_data.ix[:, 1:]
	y_train = train_data.ix[:, :1]

	X_test = test_data.ix[:, 1:]
	y_test = test_data.ix[:, :1]
	# print(X_train)
	# print(y_train)


	X_train_dict = X_train.to_dict(orient='records')
	X_test_dict = X_test.to_dict(orient='records')

	y_train_list = []
	for temp in y_train.values:
	    y_train_list.append(temp[0])
	y_test_list = []
	for temp in y_test.values:
	    y_test_list.append(temp[0])

	vectorizer = DV(sparse=False)
	vec_x_cat_train = vectorizer.fit_transform(X_train_dict)
	vec_x_cat_test = vectorizer.transform(X_test_dict)
	# print(vectorizer.inverse_transform(vec_x_cat_test))

	################################################################
	model = GaussianNB()
	model.fit(vec_x_cat_train, y_train_list)
	# make predictions
	expected = y_test_list
	predicted = model.predict(vec_x_cat_test)
	# summarize the fit of the model
	print("GAUSSIAN MODEL")
	print(metrics.classification_report(expected, predicted))
	print(metrics.confusion_matrix(expected, predicted))
	
	################################################################
	model = MultinomialNB()
	model.fit(vec_x_cat_train, y_train_list)
	# make predictions
	expected = y_test_list
	predicted = model.predict(vec_x_cat_test)
	# summarize the fit of the model
	print("MULTINOMIAL MODEL")
	print(metrics.classification_report(expected, predicted))
	print(metrics.confusion_matrix(expected, predicted))
	
	################################################################
	model = BernoulliNB()
	model.fit(vec_x_cat_train, y_train_list)
	# make predictions
	expected = y_test_list
	predicted = model.predict(vec_x_cat_test)
	# summarize the fit of the model
	print("BERNOULLI MODEL")
	print(metrics.classification_report(expected, predicted))
	print(metrics.confusion_matrix(expected, predicted))
	
	################################################################
	model = LogisticRegression()
	model.fit(vec_x_cat_train, y_train_list)
	# make predictions
	expected = y_test_list
	predicted = model.predict(vec_x_cat_test)
	# summarize the fit of the model
	print("LOGISTIC REGRESSION")
	print(metrics.classification_report(expected, predicted))
	print(metrics.confusion_matrix(expected, predicted))
	
	################################################################
	model = DecisionTreeClassifier()
	model.fit(vec_x_cat_train, y_train_list)
	# make predictions
	expected = y_test_list
	predicted = model.predict(vec_x_cat_test)
	# summarize the fit of the model
	print("DECISION TREE CLASSIFIER")
	print(metrics.classification_report(expected, predicted))
	print(metrics.confusion_matrix(expected, predicted))
	
	################################################################
	model = ExtraTreeClassifier()
	model.fit(vec_x_cat_train, y_train_list)
	# make predictions
	expected = y_test_list
	predicted = model.predict(vec_x_cat_test)
	# summarize the fit of the model
	print("EXTRA TREE CLASSIFIER")
	print(metrics.classification_report(expected, predicted))
	print(metrics.confusion_matrix(expected, predicted))
	

if __name__ == '__main__':
	main()
