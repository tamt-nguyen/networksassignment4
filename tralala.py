import networkx as nx
import matplotlib.pyplot as plt
from collections import defaultdict
import heapq
import random
import os
import sys
#66a1b8e2bed32c6798814209b781513dda71ac95c51f7117a654a3545f3d0928

class SDNController:
    def __init__(self):
        self.network = nx.Graph()
        self.flow_table = defaultdict(list)
        self.backup_paths = {}

    def add_node(self, node_id):
        self.network.add_node(node_id)
        print(f"Node {node_id} added.")

    def add_link(self, node1, node2, weight=1):
        self.network.add_edge(node1, node2, weight=weight)
        print(f"Link added between {node1} and {node2} with weight {weight}.")

    def remove_link(self, node1, node2):
        if self.network.has_edge(node1, node2):
            self.network.remove_edge(node1, node2)
            print(f"Link between {node1} and {node2} removed.")
        else:
            print(f"No link exists between {node1} and {node2}.")

    def compute_shortest_path(self, src, dst):
        try:
            path = nx.shortest_path(self.network, src, dst, weight='weight')
            return path
        except nx.NetworkXNoPath:
            print(f"No path exists between {src} and {dst}.")
            return None

    def add_flow(self, src, dst, priority=1):
        path = self.compute_shortest_path(src, dst)
        if path:
            self.flow_table[(src, dst)].append((priority, path))
            print(f"Flow added from {src} to {dst} with priority {priority}.")
        else:
            print(f"Failed to add flow from {src} to {dst}.")

    def simulate_failure(self, node1, node2):
        self.remove_link(node1, node2)
        for (src, dst), flows in self.flow_table.items():
            for priority, path in flows:
                if node1 in path and node2 in path:
                    print(f"Recomputing path for flow from {src} to {dst}...")
                    new_path = self.compute_shortest_path(src, dst)
                    if new_path:
                        self.flow_table[(src, dst)].append((priority, new_path))
                        print(f"Flow from {src} to {dst} rerouted.")
                    else:
                        print(f"No alternate path found for flow from {src} to {dst}.")

    def visualize_network(self):
        plt.figure(figsize=(8, 6))
        pos = nx.spring_layout(self.network)
        nx.draw(self.network, pos, with_labels=True, node_size=700, node_color='lightblue', font_size=12, font_weight='bold', edge_color='gray')
        plt.title("Network Topology")
        plt.show()

    def show_flow_table(self):
        for key, paths in self.flow_table.items():
            print(f"Flow from {key[0]} to {key[1]}:")
            for priority, path in paths:
                print(f"  Priority {priority}: {path}")

    def run_cli(self):
        while True:
            try:
                print("\nSDN Controller CLI")
                print("1. Add Node")
                print("2. Add Link")
                print("3. Remove Link")
                print("4. Add Flow")
                print("5. Simulate Link Failure")
                print("6. Visualize Network")
                print("7. Show Flow Table")
                print("8. Exit")
                choice = input("Select an option: ").strip()
                if choice == '1':
                    node_id = input("Enter node ID: ").strip()
                    self.add_node(node_id)
                elif choice == '2':
                    node1 = input("Enter first node ID: ").strip()
                    node2 = input("Enter second node ID: ").strip()
                    weight = int(input("Enter link weight (default 1): ").strip())
                    self.add_link(node1, node2, weight)
                elif choice == '3':
                    node1 = input("Enter first node ID: ").strip()
                    node2 = input("Enter second node ID: ").strip()
                    self.remove_link(node1, node2)
                elif choice == '4':
                    src = input("Enter source node: ").strip()
                    dst = input("Enter destination node: ").strip()
                    priority = int(input("Enter priority (default 1): ").strip())
                    self.add_flow(src, dst, priority)
                elif choice == '5':
                    node1 = input("Enter first node ID: ").strip()
                    node2 = input("Enter second node ID: ").strip()
                    self.simulate_failure(node1, node2)
                elif choice == '6':
                    self.visualize_network()
                elif choice == '7':
                    self.show_flow_table()
                elif choice == '8':
                    print("Exiting...")
                    break
                else:
                    print("Invalid choice. Please try again.")
            except KeyboardInterrupt:
                print("\nExiting on user interrupt...")
                break
            except Exception as e:
                print(f"Unexpected error: {e}")
                break

if __name__ == "__main__":
    controller = SDNController()
    try:
        controller.run_cli()
    except KeyboardInterrupt:
        print("\nExiting on user interrupt...")
    except Exception as e:
        print(f"Unexpected error in main: {e}")

