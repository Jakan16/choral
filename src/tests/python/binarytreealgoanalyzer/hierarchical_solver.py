from bin_op_tree import Node
from solution_iterator import next_solution
import random

def score_ratio(leaf, inner):
    return (leaf / inner) if inner > 0 else 0

def score_diff(leaf, inner):
    return leaf - inner

def score_leaf(leaf, inner):
    return leaf

def score_inner(leaf, inner):
    return (1/inner) if inner > 0 else 0

def hierarchical(tree, numcolors, score = score_ratio, oneShot = False, foresting = True, pruning_factor = 1, pruning_inner_factor = 0):
    sols = []
    for color in range(numcolors):
        tree.clear_innner_colors()
        coalesce_preferred(tree)
        hierarchy = [color]
        score_wrapper = lambda x: score(x[1][0], x[1][1])
        candidateScores = lambda pf: role_occurrences_partial_hierarchy(tree, numcolors, hierarchy_set if foresting else set(), pf)
        if oneShot:
            hierarchy_set = set(hierarchy)
            rest = map(lambda x: x[0], sorted(filter(lambda x: x[0] not in hierarchy_set, candidateScores((pruning_factor, pruning_inner_factor))), key=score_wrapper, reverse=True))
            hierarchy.extend(rest)
        else:
            while len(hierarchy) < numcolors:
                hierarchy_set = set(hierarchy)
                next = max(filter(lambda x: x[0] not in hierarchy_set, candidateScores((pruning_factor, pruning_inner_factor))), key=score_wrapper)[0]
                hierarchy.append(next)
        copy_parent_color_hierarchy(tree, color, hierarchy)
        sols.append(tree.sol_cost() + (0 if tree.getColor() == color else 1))
    return sols

random_instance = random.Random()
random_instance.seed(442)
def randomHierarchical(tree, numcolors):
    sols = []
    hierarchy = list(range(numcolors))
    random_instance.shuffle(hierarchy)
    for color in range(numcolors):
        tree.clear_innner_colors()
        coalesce_preferred(tree)
        copy_parent_color_hierarchy(tree, color, hierarchy)
        sols.append(tree.sol_cost() + (0 if tree.getColor() == color else 1))
    return sols

def bruteHierarchical(tree, numcolors):
    bestCosts = [float('inf')]*numcolors
    bestHierarchys = [None]*numcolors
    tree.sol_cost()
    while True:
        ishierarchical, hierarchy = is_hierarchical(tree, numcolors)
        if not ishierarchical:# or 0 not in hierarchy[0]:
            if not next_solution(tree, numcolors):
                break
            continue

        for color in range(numcolors):
            if color not in hierarchy[0]:
                continue

            # the tree is hierarchical and role 0 is at the top
            cost = tree.cost + (0 if tree.getColor() == 0 else 1)
            if cost < bestCosts[color]:
                bestCosts[color] = cost
                bestHierarchys[color] = hierarchy

        if not next_solution(tree, numcolors):
            break
    return bestCosts

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

def copy_parent_color_hierarchy(node, color, hierarchy):
    if node.left is None:
        return # skip leafs
    for h in hierarchy:
        if h in node.left.union:
            if h in node.right.union:
                node.setColor(h)
            else:
                node.setColor(color)
            break
        elif h in node.right.union:
            node.setColor(color)
            break
    copy_parent_color_hierarchy(node.left, node.getColor(), hierarchy)
    copy_parent_color_hierarchy(node.right, node.getColor(), hierarchy)

def role_occurrences_partial_hierarchy(node, numcolors, exempt, pruning_factor):
    count = [[0, 0] for i in range(numcolors)]
    color = role_occurrences_partial_hierarchy_aux(node, count, exempt, pruning_factor)
    if color >= 0:
        count[color][0] += 1
    return enumerate(count)

def role_occurrences_partial_hierarchy_aux(node, count, exempt, pruning_factor):
    if node.left is None: # leaf
        #return node.getColor()
        return -1
    if not node.union.isdisjoint(exempt):
        role_occurrences_partial_hierarchy_aux(node.left, count, exempt, pruning_factor)
        role_occurrences_partial_hierarchy_aux(node.right, count, exempt, pruning_factor)
        return -1
    if len(node.union) == node.size:
        return -1
    return role_occurrences_count(node, count, pruning_factor)

def role_occurrences_count(node, count, pruning_factor):
    if node.left is None: # leaf
        return node.getColor()

    color1 = role_occurrences_count(node.left, count, pruning_factor)
    color2 = role_occurrences_count(node.right, count, pruning_factor)

    def count_val():
        return pruning_factor[0] if len(node.union) == node.size else 1

    if len(node.union) > 1:
        for color in node.union:
            count[color][1] += pruning_factor[1] if len(node.union) == node.size else 1

    if color1 == color2:
        return color1
    if color1 >= 0:
        count[color1][0] += count_val()
    if color2 >= 0:
        count[color2][0] += count_val()
    return -1

def is_hierarchical(node, numcolors):
    ref = [x for x in range(numcolors)]
    dep = [set() for x in range(numcolors)]
    is_hierarchical_aux(node, dep)

    res = []

    while len(dep) > 0:
        toRemove = []
        # find all empty set
        for i, d in enumerate(dep):
            if len(d) == 0:
                toRemove.append(i)
        # no empty set means a cycle
        if len(toRemove) == 0:
            return False, res
        roles = []
        for i in reversed(toRemove):
            roles.append(ref[i])
            # delete from lists
            del ref[i]
            del dep[i]
        # add them to the result
        res.append(list(reversed(roles)))

        # remove the role from all sets
        for d in dep:
            for role in roles:
                d.discard(role)
    return True, res

def is_hierarchical_aux(node, dep):
    if node.left is None:
        return False # skip leafs

    is_hierarchical_aux(node.left, dep)
    is_hierarchical_aux(node.right, dep)

    if node.getColor() != node.left.getColor():
        dep[node.left.getColor()].add(node.getColor())

    if node.getColor() != node.right.getColor():
        dep[node.right.getColor()].add(node.getColor())

if __name__ == '__main__':
    numRoles = 4
    numLeafs = 10
    tree = Node.generate_tree(numRoles, numLeafs)
    #sols = hierarchical(tree, numRoles)
    sols = bruteHierarchical(tree, numRoles)
    print(tree)
    print(tree.sol_str())
    print(sols)
