from optimal_solver import optimal
from brute_optimal_solver import bruteOptimal
from bin_op_tree import Node
import random

if __name__ == '__main__':

    random_generator = random.Random()
    random_generator.seed(42)

    for i in range(100):
        print(i)

        numRoles = random_generator.randint(2,7)
        numLeafs = random_generator.randint(2,12)

        tree = Node.generate_tree(numRoles, numLeafs)

        sols_op = optimal(tree, numRoles)
        tree.clear_innner_colors(0)
        sols_brute = bruteOptimal(tree, numRoles)
        sols_cost = list(tree.solve_tree(numRoles))

        if not (sols_op == sols_brute and sols_brute == sols_cost):
            print(tree)
            print(sols_op)
            print(sols_brute)
            print(sols_cost)
            raise "Optimal costs are not equal"
