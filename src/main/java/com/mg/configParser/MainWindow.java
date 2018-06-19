package com.mg.configParser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;

import java.awt.Font;
import java.awt.Insets;

public class MainWindow extends JFrame {
	private JTextField tfPath;
	private static JTextArea taProgress;
	public MainWindow() {
		super("MW Config Parser");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		setSize(490, 300);
		JLabel lblDirectory = new JLabel("Directory : ");
		lblDirectory.setFont(new Font("SansSerif", Font.PLAIN, 15));
		lblDirectory.setBounds(14, 12, 83, 18);
		getContentPane().add(lblDirectory);
		
		tfPath = new JTextField();
		tfPath.setFont(new Font("SansSerif", Font.PLAIN, 15));
		tfPath.setBounds(86, 9, 185, 24);
		getContentPane().add(tfPath);
		tfPath.setColumns(10);
		
		JButton btnBrowse = new JButton("Select Dir");
		btnBrowse.setFont(new Font("SansSerif", Font.PLAIN, 15));
		btnBrowse.setBounds(277, 8, 95, 27);
		btnBrowse.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int result = fc.showOpenDialog(new JFrame());
				if(result == JFileChooser.APPROVE_OPTION){
					String path = fc.getSelectedFile().getAbsolutePath();
					tfPath.setText(path);
				}
			}
		});
		getContentPane().add(btnBrowse);
		
		JButton btnStart = new JButton("Start");
		btnStart.setFont(new Font("SansSerif", Font.PLAIN, 15));
		btnStart.setBounds(376, 8, 83, 27);
		btnStart.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String path = tfPath.getText();
				if(path==null){
					//show alert
				}else{
					Main.parse(path);
				}
			}
		});
		getContentPane().add(btnStart);
		
		taProgress = new JTextArea();
		taProgress.setMargin(new Insets(1, 5, 1, 5));
		taProgress.setFont(new Font("SansSerif", Font.PLAIN, 15));
		taProgress.setEditable(false);
		taProgress.setBounds(14, 42, 444, 199);
		JScrollPane sp = new JScrollPane(taProgress);
		sp.setBounds(14, 44, 445, 200);
		getContentPane().add(sp);
		sp.setVisible(true);
		//getContentPane().add(taProgress);			
		setVisible(true);
	}
	public static void setProgress(String t){
		taProgress.append(t+"\n");
		taProgress.setCaretPosition(taProgress.getDocument().getLength());
	}
	public static void clearProgress(){
		taProgress.setText("");
	}
}
