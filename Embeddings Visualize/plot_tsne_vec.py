import pickle
import plotly
import plotly.graph_objs as go
import codecs


def main():
	fv1 = codecs.open('tmp1.txt', 'r', 'utf-8')
	fv2 = codecs.open('tmp2.txt', 'r', 'utf-8')
	vec0 = list()
	vec1 = list()
	for line in fv1:
		vec0.append(float(line))
	for line in fv2:
		vec1.append(float(line))
	fl = codecs.open('word2vec-labels.txt', 'r', 'utf-8')
	labels = list()
	for line in fl:
		labels.append(line)
	trace = go.Scatter(
	    x=vec0[0:200],
	    y=vec1[0:200],
	    mode='markers+text',
	    text=labels[0:200],
	    textposition='bottom'
	)
	layout = go.Layout(
    	showlegend=False
	)
	data = [trace]
	fig = go.Figure(data=data, layout=layout)
	plot_url = plotly.offline.plot(fig, filename='word2vec-basic.html')


if __name__ == '__main__':
	main()
