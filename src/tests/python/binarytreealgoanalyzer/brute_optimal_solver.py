from bin_op_tree import Node
from solution_iterator import next_solution

def bruteOptimal(tree, numcolors):
    tree.sol_cost()
    best_sols = [float('inf')]*numcolors
    while True:
        rootColor = tree.getColor()
        cost = tree.cost
        assert tree.cost == tree.sol_cost()
        for color, best_sol in enumerate(best_sols):
            if color == rootColor:
                finalCost = cost
            else:
                finalCost = cost + 1
            if finalCost < best_sol:
                best_sols[color] = finalCost
        if not next_solution(tree, numcolors):
            break
    return best_sols

if __name__ == '__main__':
    numRoles = 4
    numLeafs = 10
    for i in range(100):
        if i%10 == 0:
            print(i)
        tree = Node.generate_tree(numRoles, numLeafs)
        sols = bruteOptimal(tree, numRoles)
    print(tree)
    print(tree.sol_str())
    print(sols)
