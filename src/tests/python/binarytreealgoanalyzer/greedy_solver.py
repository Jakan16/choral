from bin_op_tree import Node

def greedy(tree, numcolors):
    sols = []
    greedy_aux(tree)
    uncoloredRoot = tree.getColor() == None
    for color in range(numcolors):
        if uncoloredRoot:
            tree.setColor(color)
        cost = tree.sol_cost()
        if tree.getColor() is not color:
            cost += 1
        sols.append(cost)
    return sols

def greedy_aux(tree):
    for node in tree.postorder_walk():
        if node.left is None:
            continue # skip leafs
        lColor = node.left.getColor()
        rColor = node.right.getColor()
        if lColor != None: # left is colored
            if rColor == lColor : # both children is same color
                node.coalesce(node.left)
            elif rColor == None: # only left is colored
                node.coalesce(node.left)
                node.right.coalesce(node.left)
            # else: pass # both differnt colors, do nothing
        else: # left is uncolored
            node.coalesce(node.right)
            node.left.coalesce(node.right)

if __name__ == '__main__':
    numRoles = 4
    numLeafs = 10
    tree = Node.generate_tree(numRoles, numLeafs)
    sols = greedy(tree, numRoles)
    print(tree)
    print(tree.sol_str())
    print(sols)
