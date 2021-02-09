import random

roleNames = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U']

class MutableColor:
    def __init__(self, default = None):
        self.color = default
        self.parent = None

    def coalesce(self, other):
        self.parent = other

    def getColor(self):
        if self.parent == None:
            return self
        self.parent = self.parent.getColor()
        return self.parent

class Node:
    def __init__(self):
        self.color = MutableColor(0)
        self.left = None
        self.right = None
        self.num = None
        self.preferred = set()
        self.union = set()
        self.intersect = None
        self.cost = 0
        self.bestCost = float('inf')
        self.size = 1

    def setColor(self, color):
        self.color.getColor().color = color

    def getColor(self):
        return self.color.getColor().color

    def coalesce(self, other):
        self.color.getColor().coalesce(other.color.getColor())

    def postorder_walk(self):
        if self.left is not None:
            yield from self.left.postorder_walk()
            yield from self.right.postorder_walk()
        yield self

    def clear_innner_colors(self, color = None):
        for node in self.postorder_walk():
            if node.left is None:
                continue # skip leafs
            node.color = MutableColor(color)

    """
    Pure way to find optimal cost
    """
    def solve_tree(self, numRoles):
        if self.left is None: # leaf
            res = [1]*numRoles
            res[self.getColor()] = 0
            return res

        sol_left = self.left.solve_tree(numRoles)
        sol_right = self.right.solve_tree(numRoles)

        res = list(map(sum, zip(sol_left, sol_right)))
        minimum = min(res)
        return map(lambda x: min(minimum + 1, x) , res)

    def sol_cost(self):
        assert self.getColor() != None
        if self.left is None: # leaf
            return 0
        cost = self.left.sol_cost() + self.right.sol_cost()
        if self.getColor() is not self.left.getColor():
            cost += 1
        if self.getColor() is not self.right.getColor():
            cost += 1
        self.cost = cost
        self.bestCost = cost
        return cost

    def __str__(self):
        if self.left is None:
            return str(self.num) + '@' + roleNames[self.color.color]
        else:
            return '(' + str(self.left) + ' + ' + str(self.right) + ')'

    def sol_str(self):
        if self.left is None:
            return roleNames[self.color.color]
        else:
            return roleNames[self.color.color] + \
            '(' + self.left.sol_str() + '+' + self.right.sol_str() + ')'

    def update_union(self):
        if self.left is None:
            self.union = {self.getColor()}
            return;
        self.left.update_union()
        self.right.update_union()
        self.union = self.left.union.union(self.right.union)

    @staticmethod
    def generate_tree(numRoles, numLeafs, random_instance = None):

        if random_instance == None:
            random_instance = random

        root = Node()
        leafs = [root]

        while len(leafs) < numLeafs:
            # remove random leaf
            leaf = leafs.pop(random_instance.randrange(len(leafs)))
            leaf.left = Node()
            leaf.right = Node()
            leafs.extend((leaf.left, leaf.right))

        for i, leaf in enumerate(leafs):
            leaf.setColor(random_instance.randrange(numRoles))
            leaf.num = i
        return root
