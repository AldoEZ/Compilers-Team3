package mx.unam.fi.compilers.g5.team03.parser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class ParseTreeViewer extends JFrame {
    /**
     * tree -> variable that displays information in the form of a hierarchical tree
     * searchField -> text box for searching nodes
     * resultLabel -> label that displays the search result
     * matches -> all matches found
     * currentMatchIndex -> current match in the tree
     */
    private final JTree tree;
    private final JTextField searchField;
    private final JLabel resultLabel;
    
    private final List<TreePath> matches;
    private int currentMatchIndex;
    
    public ParseTreeViewer(ParseTreeNode root) {
        setTitle("Parse Tree Viewer");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        this.matches = new ArrayList<>();
        this.currentMatchIndex = -1;
        
        DefaultMutableTreeNode swingRoot = buildTree(root);
        tree = new JTree(swingRoot);
        
        tree.setFont(new Font("Consolas", Font.PLAIN, 16));
        tree.setRowHeight(24);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(true);
        tree.setBackground(new Color(245, 245, 245));
        tree.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        expandAll();
        
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean selected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus
            ) {
                Component c = super.getTreeCellRendererComponent(
                    tree, value, selected, expanded, leaf, row, hasFocus
                );
                
                String text = value.toString();
                
                if(text.contains(" : ")) {
                    setForeground(new Color(25, 25, 112));
                    setFont(new Font("Consolas", Font.PLAIN, 15));
                } else {
                    setForeground(new Color(139, 0, 0));
                    setFont(new Font("Consolas", Font.BOLD, 15));
                }
                
                if(selected) {
                    setBackgroundSelectionColor(new Color(180, 205, 255));
                    setTextSelectionColor(Color.BLACK);
                }
                setBackgroundNonSelectionColor(new Color(245, 245, 245));
                return c;
            }
        };
        
        tree.setCellRenderer(renderer);
        
        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.getViewport().setBackground(new Color(245, 245, 245));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        JButton expandButton = new JButton("Expand All");
        expandButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        expandButton.addActionListener(e -> expandAll());
        
        JButton collapseButton = new JButton("Collapse All");
        collapseButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        collapseButton.addActionListener(e -> collapseAll());
        
        searchField = new JTextField(20);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField.addActionListener(e -> searchNodes());
        
        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchButton.addActionListener(e -> searchNodes());
        
        JButton nextButton = new JButton("Next");
        nextButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        nextButton.addActionListener(e -> nextMatch());
        
        JButton prevButton = new JButton("Previous");
        prevButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        prevButton.addActionListener(e -> previousMatch());
        
        resultLabel = new JLabel("No search");
        resultLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        topPanel.add(expandButton);
        topPanel.add(collapseButton);
        topPanel.add(new JLabel("Search node:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(prevButton);
        topPanel.add(nextButton);
        topPanel.add(resultLabel);
        
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private DefaultMutableTreeNode buildTree(ParseTreeNode node) {
        String label;
        
        if (node.getLexeme() != null) {
            label = node.getSymbol() + " : " + node.getLexeme();
        } else label = node.getSymbol();
        
        DefaultMutableTreeNode swingNode = new DefaultMutableTreeNode(label);
        
        for (ParseTreeNode child : node.getChildren())
            swingNode.add(buildTree(child));
        
        return swingNode;
    }
    
    private void expandAll() {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        expandNode(new TreePath(root));
    }
    
    private void expandNode(TreePath parent) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        
        if(node.getChildCount() >= 0) {
            for(Enumeration<?> e = node.children(); e.hasMoreElements();) {
                TreeNode child = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(child);
                expandNode(path);
            }
        }
        tree.expandPath(parent);
    }
    
    private void collapseAll() {
        for(int i = tree.getRowCount() - 1; i > 0; i--)
            tree.collapseRow(i);
        
        tree.expandRow(0);
    }
    
    private void searchNodes() {
        String query = searchField.getText().trim().toLowerCase();
        
        matches.clear();
        currentMatchIndex = -1;
        
        if (query.isEmpty()) {
            resultLabel.setText("Empty search");
            tree.clearSelection();
            return;
        }
        DefaultMutableTreeNode root =
            (DefaultMutableTreeNode) tree.getModel().getRoot();
        
        searchRecursive(new TreePath(root), query);
        
        if(matches.isEmpty()) {
            resultLabel.setText("0 matches");
            tree.clearSelection();
            return;
        }
        currentMatchIndex = 0;
        focusMatch();
    }
    
    private void searchRecursive(TreePath path, String query) {
        DefaultMutableTreeNode node =
            (DefaultMutableTreeNode) path.getLastPathComponent();
        
        String text = node.getUserObject().toString().toLowerCase();
        
        if(text.contains(query)) matches.add(path);
        
        for(int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child =
                (DefaultMutableTreeNode) node.getChildAt(i);
            searchRecursive(path.pathByAddingChild(child), query);
        }
    }
    
    private void nextMatch() {
        if(matches.isEmpty()) return;
        
        currentMatchIndex = (currentMatchIndex + 1) % matches.size();
        focusMatch();
    }
    
    private void previousMatch() {
        if(matches.isEmpty()) return;
        
        currentMatchIndex--;
        if(currentMatchIndex < 0) currentMatchIndex = matches.size() - 1;
        
        focusMatch();
    }
    
    private void focusMatch() {
        if (currentMatchIndex < 0 || currentMatchIndex >= matches.size()) return;
        
        TreePath path = matches.get(currentMatchIndex);
        
        tree.expandPath(path);
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
        
        resultLabel.setText(
            "Match " + (currentMatchIndex + 1) + " of " + matches.size()
        );
    }
    
    public static void showTree(ParseTreeNode root) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println("Could not apply system look and feel.");
            }
            
            ParseTreeViewer viewer = new ParseTreeViewer(root);
            viewer.setVisible(true);
        });
    }
}
