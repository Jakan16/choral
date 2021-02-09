def next_solution(node, numRoles):
    if node.left is None:
        return False # skip leafs
    color = (node.getColor() + 1) % numRoles
    node.setColor( color )

    stop = color is not 0 or next_solution( node.left, numRoles ) or next_solution( node.right, numRoles )
    cost = node.left.cost + node.right.cost
    if node.getColor() is not node.left.getColor():
        cost += 1
    if node.getColor() is not node.right.getColor():
        cost += 1
    node.cost = cost
    return stop
