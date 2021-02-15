import os
import errno
import random
from concurrent.futures import ProcessPoolExecutor, as_completed, ThreadPoolExecutor

import hierarchical_solver as h
import optimal_solver as op
import send_direct_solver as sd
from bin_op_tree import Node

outplut_dir = 'generated'

def optimal(tree, numRoles):
    return op.optimal(tree, numRoles)

def simple_leaf_count(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = True, foresting = False)

def leaf_count_no_inner_pruning(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = True, foresting = False, pruning_inner_factor = 1)

def simple_leaf_count_foresting(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True)

def leaf_count_foresting_one_shot_no_inner_pruning(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = True, foresting = True, pruning_inner_factor = 1)

def leaf_count_foresting(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = True, foresting = True)

def inner_count_foresting(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_inner, oneShot = False, foresting = True)

def inner_count(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_inner, oneShot = True, foresting = False)

def inner_count_no_inner_pruning(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_inner, oneShot = True, foresting = False, pruning_inner_factor = 1)

def inner_count_foresting_one_shot_no_inner_pruning(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_inner, oneShot = True, foresting = True, pruning_inner_factor = 1)

def ratio_foresting(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True)

def ratio(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = True, foresting = False)

def ratio_foresting_one_shot(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = True, foresting = True)

def diff_foresting(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_diff, oneShot = False, foresting = True)

def ratio_pruning_2_foresting(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 0.2)

def ratio_pruning_5_foresting(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 0.5)

def leaf_count_pruning_5_foresting(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 0.5)

def leaf_count_pruning_0_foresting(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 0)

def ratio_pruning_0_foresting(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 0)

def random_hierarchy(tree, numRoles):
    return h.randomHierarchical(tree, numRoles)

def brute_optimal_hierarchical(tree, numRoles):
    tree.update_union()
    return h.bruteHierarchical(tree, numRoles)

def simple_leaf_count_foresting_no_inner_pruning(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_inner_factor = 1)

def inner_count_foresting_no_inner_pruning(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_inner, oneShot = False, foresting = True, pruning_inner_factor = 1)

def ratio_foresting_no_inner_pruning(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_inner_factor = 1)

def ratio_no_inner_pruning(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = True, foresting = False, pruning_inner_factor = 1)

def ratio_foresting_one_shot_no_inner_pruning(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = True, foresting = True, pruning_inner_factor = 1)

def send_direct(tree, numRoles):
    tree.update_union()
    return sd.send_direct(tree, numRoles)

def leaf_pf_0(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 0)

def leaf_pf_1(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 0.1)

def leaf_pf_2(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 0.2)

def leaf_pf_3(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 0.3)

def leaf_pf_4(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 0.4)

def leaf_pf_5(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 0.5)

def leaf_pf_6(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 0.6)

def leaf_pf_7(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 0.7)

def leaf_pf_8(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 0.8)

def leaf_pf_8_one_shot(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = True, foresting = True, pruning_factor = 0.8)

def leaf_pf_9(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 0.9)

def leaf_pf_99(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 0.99)

def leaf_pf_10(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 1)

def leaf_pf_e(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 0.36787944117)

def leaf_pf_2e(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 2*0.36787944117)

def leaf_pf_1sube(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_leaf, oneShot = False, foresting = True, pruning_factor = 1-0.36787944117)

def ratio_pf_0(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 0)

def ratio_pf_1(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 0.1)

def ratio_pf_2(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 0.2)

def ratio_pf_3(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 0.3)

def ratio_pf_4(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 0.4)

def ratio_pf_5(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 0.5)

def ratio_pf_6(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 0.6)

def ratio_pf_7(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 0.7)

def ratio_pf_8(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 0.8)

def ratio_pf_8_one_shot(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = True, foresting = True, pruning_factor = 0.8)

def ratio_pf_9(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 0.9)

def ratio_pf_99(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 0.99)

def ratio_pf_10(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 1)

def ratio_pf_e(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 0.36787944117)

def ratio_pf_2e(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 2*0.36787944117)

def ratio_pf_1sube(tree, numRoles):
    return h.hierarchical(tree, numRoles, score = h.score_ratio, oneShot = False, foresting = True, pruning_factor = 1-0.36787944117)

def cost_generators():

    return [
        ('leaf_pf_8_one_shot', leaf_pf_8_one_shot),
        ('ratio_pf_8_one_shot', ratio_pf_8_one_shot)
    ]

    return [
        ('leaf_pf_0', leaf_pf_0),
        ('leaf_pf_1', leaf_pf_1),
        ('leaf_pf_2', leaf_pf_2),
        ('leaf_pf_3', leaf_pf_3),
        ('leaf_pf_4', leaf_pf_4),
        ('leaf_pf_5', leaf_pf_5),
        ('leaf_pf_6', leaf_pf_6),
        ('leaf_pf_7', leaf_pf_7),
        ('leaf_pf_8', leaf_pf_8),
        ('leaf_pf_9', leaf_pf_9),
        ('leaf_pf_10', leaf_pf_10),
        ('ratio_pf_0', ratio_pf_0),
        ('ratio_pf_1', ratio_pf_1),
        ('ratio_pf_2', ratio_pf_2),
        ('ratio_pf_3', ratio_pf_3),
        ('ratio_pf_4', ratio_pf_4),
        ('ratio_pf_5', ratio_pf_5),
        ('ratio_pf_6', ratio_pf_6),
        ('ratio_pf_7', ratio_pf_7),
        ('ratio_pf_8', ratio_pf_8),
        ('ratio_pf_9', ratio_pf_9),
        ('ratio_pf_10', ratio_pf_10)
    ]

    #return [
            #('simple_leaf_count_foresting_no_inner_pruning', simple_leaf_count_foresting_no_inner_pruning),
            #('inner_count_foresting_no_inner_pruning', inner_count_foresting_no_inner_pruning),
            #('ratio_foresting_no_inner_pruning', ratio_foresting_no_inner_pruning),
            #('ratio_no_inner_pruning', ratio_no_inner_pruning)
            #('brute_optimal_hierarchical', brute_optimal_hierarchical)
            #('optimal', optimal),
            #('simple_leaf_count', simple_leaf_count),
            #('foresting_leaf_count', simple_leaf_count_foresting)
            #('inner_count_foresting', inner_count_foresting),
            #('ratio_foresting', ratio_foresting),
            #('diff_foresting', diff_foresting),
            #('ratio_pruning_2_foresting', ratio_pruning_2_foresting)
            #('ratio', ratio)
            #('ratio_pruning_5_foresting', ratio_pruning_5_foresting),
            #('ratio_pruning_0_foresting', ratio_pruning_0_foresting)
            #('random_hierarchy', random_hierarchy)
            #('send_direct', send_direct)
            #('ratio_foresting_one_shot', ratio_foresting_one_shot)
            #('leaf_count_no_inner_pruning', leaf_count_no_inner_pruning),
            #('inner_count_no_inner_pruning', inner_count_no_inner_pruning)
            #('ratio_foresting_one_shot_no_inner_pruning', ratio_foresting_one_shot_no_inner_pruning),
            #('leaf_count_foresting_one_shot_no_inner_pruning', leaf_count_foresting_one_shot_no_inner_pruning),
            #('inner_count_foresting_one_shot_no_inner_pruning', inner_count_foresting_one_shot_no_inner_pruning)
            #('leaf_count_pruning_0_foresting', leaf_count_pruning_0_foresting)
        #]

def stepper(stop):
    current = 4
    while current < stop:
        yield current
        current *= 2

def create_output_folder_if_missing():
    try:
        os.mkdir(outplut_dir)
    except OSError as e:
        if e.errno != errno.EEXIST:
            raise

def genrate_and_write(name, func):
    random_instance = random.Random()
    random_instance.seed(42)

    with open(outplut_dir + '/' + name + '.txt', 'w') as file:
        for step in stepper(130):
            print(name, ' ', step)
            file.write(str(step))
            file.write('\n')
            for i in range(300):
                numRoles = numLeafs = step
                tree = Node.generate_tree(numRoles, numLeafs, random_instance)
                cost = func(tree, numRoles+1)
                cost_at_forign_role = cost.pop()
                cost = [ c for i, c in enumerate(cost) if i in tree.union ]
                cost.append(cost_at_forign_role)
                file.write(str(cost))
                file.write('\n')

def run_all():
    executor = ProcessPoolExecutor(max_workers=8)
    futures = {executor.submit(genrate_and_write, name, func) : name for name, func in cost_generators()}
    for future in as_completed(futures):
        name = futures[future]
        try:
            future.result()
        except Exception as exc:
            print('%r generated an exception: %s' % (name, exc))
        else:
            print('completed %s' % name)

def main():
    create_output_folder_if_missing()
    run_all()

if __name__ == '__main__':
    main()
