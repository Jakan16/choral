import numpy as np
import matplotlib.pyplot as plt
from functools import reduce

input_folder = 'execution_times'

def read_file(input_file):
    with open(input_folder + '/' + input_file + '.txt', 'r') as file:
        return [[int(i) for i in line.strip()[1:-1].split(', ')] for line in file.readlines()]

def plot_sorted_average(data):
    sum = reduce(lambda x, y: [i[0] + i[1] for i in zip(x, sorted(y, reverse=True))], data, [0]*len(data[0]))
    y = [s/(len(data)*1000000) for s in sum]
    x = list(range(1, len(data[0])+1))
    plt.plot(x, y)

#files = ['direct', 'pruning_foresting_hierarchy', 'leaf_count']
#files = ['direct_busy', 'leaf_count_busy', 'optimal_busy']
#files = ['direct_wait', 'leaf_count_wait', 'optimal_wait']
#files = ['direct_inc_wait', 'leaf_count_inc_wait', 'optimal_inc_wait']
files = ['direct_delayed_msg', 'leaf_count_delayed_msg', 'optimal_delayed_msg']
#files = ['direct_delayed_msg', 'leaf_count_delayed_msg']
#files = ['optimal_no_q', 'leaf_count_no_q', 'direct_no_q']

for file in files:
    plot_sorted_average(read_file(file))

#plot_sorted_average(read_file('direct'))
#plot_sorted_average(read_file('pruning_foresting_hierarchy'))
#plot_sorted_average(read_file('leaf_count'))
#plot_sorted_average(read_file('leaf_count_iterative'))
#plot_sorted_average(read_file('optimal'))
plt.xlabel('roles')
plt.ylabel('running time - ms')
plt.legend(['direct', 'leaf count', 'optimal'])
plt.show()
