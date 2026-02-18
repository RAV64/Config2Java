def sibling = load('foo.groovy')
def parent = load('../bar.groovy')
def child = load('./folder/baz.groovy')

return [
    mode: sibling.mode,
    name: sibling.name + '-' + parent.name + '-' + child.name
]
