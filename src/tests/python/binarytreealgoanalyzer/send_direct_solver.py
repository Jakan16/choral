from bin_op_tree import Node

def send_direct(tree, numcolors):
    sols = []
    for rootcolor in range(numcolors):
        cost, color = send_direct_aux(tree, rootcolor)
        if color != rootcolor:
            cost += 1
        sols.append(cost)
    return sols

def send_direct_aux(node, rootColor):
    if node.left is None:
        return (0, node.getColor())

    cost_left, color_left = send_direct_aux(node.left, rootColor)
    cost_right, color_right = send_direct_aux(node.right, rootColor)

    if color_left == color_right:
        return (cost_left + cost_right, color_left)
    else:
        extra_cost = (1 if color_left != rootColor else 0) + (1 if color_right != rootColor else 0)
        return (cost_left + cost_right + extra_cost, rootColor)

if __name__ == '__main__':
    numRoles = 1
    numLeafs = 4
    tree = Node.generate_tree(numRoles, numLeafs)
    sols = send_direct(tree, numRoles + 1)
    print(tree)
    print(sols)
