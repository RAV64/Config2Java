def data = load('./child.groovy')
data.name = data.name + '-nested'
return data
