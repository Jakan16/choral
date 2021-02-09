from bin_op_tree import Node

def optimal(tree, numcolors):
    sols = []
    for color in range(numcolors):
        tree.clear_innner_colors()
        preferred = coalesce_preferred(tree)
        copy_parent_color(tree, color)
        cost = tree.sol_cost()
        if tree.getColor() is not color:
            cost += 1
        sols.append(cost)
    return sols

def coalesce_preferred(node):
    if node.left is None:
        node.union = {node.getColor()}
        return set([node.color.color])
    left_set = coalesce_preferred(node.left)
    right_set = coalesce_preferred(node.right)
    intersect = left_set.intersection(right_set)
    node.union = node.left.union.union(node.right.union)
    node.size = (node.left.size + node.right.size) if len(node.union) > 1 else 1

    if len(intersect) == 0:
        node.preferred = left_set.union(right_set)
        node.intersect = False
        return node.preferred
    if len(intersect) == 1:
        node.setColor(next(iter(intersect)))
    node.preferred = intersect
    node.intersect = True
    return intersect

def copy_parent_color(node, color):
    if node.left is None:
        assert node.right is None
        return # skip leafs
    if node.getColor() == None:
        if color in node.preferred or not node.intersect:
            node.setColor(color)
        else:
            node.setColor(next(iter(node.preferred)))
        #node.setColor(color)
    copy_parent_color(node.left, node.getColor())
    copy_parent_color(node.right, node.getColor())

if __name__ == '__main__':
    numRoles = 4
    numLeafs = 10
    tree = Node.generate_tree(numRoles, numLeafs)
    sols = optimal(tree, numRoles)
    print(tree)
    print(tree.sol_str())
    print(sols)
