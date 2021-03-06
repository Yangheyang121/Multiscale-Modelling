/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multiscalemodelling;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
//import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;

/**
 *
 * @author hacio
 */
public class MultiscaleModellingFrame extends javax.swing.JFrame {

    int width, height;
    int grains, inclusions, sizeOfInclusions;
    int randomX, randomY;
    boolean play;
    Cell[][] matrix, matrix2, matrixED;
    //Image offScrImg;
    BufferedImage offScrImg;
    //Graphics offScrGraph;
    Graphics2D offScrGraph;
    boolean isFull;
    //int counter;
    //BufferedImage image;
    Map<Integer, Integer> neighbours = new HashMap<Integer, Integer>();
    List<HashMap.Entry<Integer, Integer>> mostFrequent;
    int GBsize = 0;
    boolean isMC = false;
    int stateNumber = 0;
    List<Color> stateNumberList = new ArrayList<Color>();
    int randomColor;
    boolean playMC;
    int done;
    int energy, energyAfter, energyBefore;
    Cell cellBefore, cellAfter;
    int iterationMC = 0;
    Color colorBefore, colorAfter;
    int counter = 0;
    Color dualPhaseColor;
    List<Color> stateNumberList2 = new ArrayList<Color>();
    boolean switched = false;

    /**
     * Creates new form MultiscaleMdoellingFrame
     */
    public MultiscaleModellingFrame() {

        initComponents();
        startButton.setEnabled(false);
        jProgressBar1.setStringPainted(true);
        jProgressBar1.setString(0 + "% Complete");
        addGrainsButton.setEnabled(false);
        addInclusionsButton.setEnabled(false);
        GBAllGrainsButton.setEnabled(false);
        clearSpaceButton.setEnabled(false);
        play = false;
        playMC = false;
        isFull = false;
        //offScrImg = createImage(jPanel1.getWidth(), jPanel1.getHeight());
        offScrImg = new BufferedImage(jPanel1.getWidth(), jPanel1.getHeight(), BufferedImage.TYPE_INT_RGB);
        //offScrGraph = offScrImg.getGraphics();
        offScrGraph = offScrImg.createGraphics();
        offScrGraph.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY));

        Timer time = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                if (play) {
                    if (typeOfNeighborhoodComboBox.getSelectedItem().toString().equals("von Neumann")) {
                        vonNeumann();
                    } else if (typeOfNeighborhoodComboBox.getSelectedItem().toString().equals("Moore")) {
                        moore();
                    } else if (typeOfNeighborhoodComboBox.getSelectedItem().toString().equals("Moore 2")) {
                        moore2();
                    }
                    refresh();
                    isFullMatrix();
                    checkProgress();
                }
                if (playMC && (iterationMC > 0)) {
                    monteCarlo();
                    iterationMC--;
                    counter++;
                    refresh();
                    System.out.println("iteracja: " + iterationMC);
                    checkProgressMC();
                }
                if (playMC && iterationMC == 0) {
                    //isFullMatrix();
                    playMC = false;
                    startButton.setText("Finished");
                    //startButton.setEnabled(false);
                    addGrainsButton.setEnabled(false);
                    addInclusionsButton.setEnabled(true);
                    GBAllGrainsButton.setEnabled(true);
                    GBSingleGrainsButton.setEnabled(true);
                    //play = false;
                    //startButton.setText("Finished");
                    // startButton.setEnabled(false);*/
                }
            }
        };
        time.scheduleAtFixedRate(task, 0, 100);
        refresh();
    }

    void checkProgress() {
        int counter = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (matrix[i][j].id != -1) {
                    counter++;
                }
            }
        }
        jProgressBar1.setValue((int) ((counter * 100) / (width * height)));
        if ((int) ((counter * 100) / (width * height)) == 100) {
            jProgressBar1.setString("100% Completed!");
        } else {
            jProgressBar1.setString((int) ((counter * 100) / (width * height)) + "% Complete");
        }
    }

    void checkProgressMC() {
        int mcs = Integer.parseInt(MCSTextField.getText());
        jProgressBar1.setValue((int) ((counter * 100) / mcs));
        if ((int) ((counter * 100) / mcs) == 100) {
            jProgressBar1.setString("100% Completed!");
        } else {
            jProgressBar1.setString((int) ((counter * 100) / mcs) + "% Complete");
        }
    }

    void checkPercentageOfGB() {
        int counter = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (matrix[i][j].color == Color.BLACK) {
                    counter++;
                }
            }
        }
        //jProgressBar1.setValue((int) ((counter * 100) / (width * height)));
        perOfGBLabel.setText((int) ((counter * 100) / (width * height)) + "% of GB");
    }

    void refresh() {
        offScrGraph.setColor(jPanel1.getBackground());
        offScrGraph.fillRect(0, 0, jPanel1.getWidth(), jPanel1.getHeight());
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //if (matrix[i][j].id != -1) {
                offScrGraph.setColor(matrix[i][j].color);
                int x = i * jPanel1.getWidth() / width;
                int y = j * jPanel1.getHeight() / height;
                offScrGraph.fillRect(x, y, jPanel1.getWidth() / width, jPanel1.getHeight() / height);
                //}
            }
        }
        /* offScrGraph.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < height; i++) {
            int y = i * jPanel1.getHeight() / height;
            offScrGraph.drawLine(0, y, jPanel1.getWidth(), y);
        }
        for (int i = 0; i < width; i++) {
            int x = i * jPanel1.getWidth() / width;
            offScrGraph.drawLine(x, 0, x, jPanel1.getHeight());
        }*/
        //offScrImg = new BufferedImage(jPanel1.getWidth(), jPanel1.getHeight(), BufferedImage.TYPE_INT_RGB);
        //offScrGraph = offScrImg.getGraphics();
        jPanel1.getGraphics().drawImage(offScrImg, 0, 0, jPanel1);
    }

    void refresh2() {
        offScrGraph.setColor(jPanel1.getBackground());
        offScrGraph.fillRect(0, 0, jPanel1.getWidth(), jPanel1.getHeight());
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //if (matrix[i][j].id != -1) {
                offScrGraph.setColor(matrixED[i][j].color);
                int x = i * jPanel1.getWidth() / width;
                int y = j * jPanel1.getHeight() / height;
                offScrGraph.fillRect(x, y, jPanel1.getWidth() / width, jPanel1.getHeight() / height);
                //}
            }
        }
        jPanel1.getGraphics().drawImage(offScrImg, 0, 0, jPanel1);
    }

    /**
     * This method is called from within the constructor to the form. WARNING:
     * Do NOT modify this code. The content of this method is always regenerated
     * by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        startButton = new javax.swing.JButton();
        createButton = new javax.swing.JButton();
        addGrainsButton = new javax.swing.JButton();
        numbersOfGrainsTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        amountOfInclusionsTextField = new javax.swing.JTextField();
        sizeOfInclusionsTextField = new javax.swing.JTextField();
        typeOfInclusionComboBox = new javax.swing.JComboBox<>();
        addInclusionsButton = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        widthTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        heightTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        typeOfNeighborhoodComboBox = new javax.swing.JComboBox<>();
        GBAllGrainsButton = new javax.swing.JButton();
        clearSpaceButton = new javax.swing.JButton();
        perOfGBLabel = new javax.swing.JLabel();
        GBsizeLabel = new javax.swing.JLabel();
        GBSingleGrainsButton = new javax.swing.JButton();
        generateMonteCarlo = new javax.swing.JButton();
        startMonteCarlo = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        stateNumberTextField = new javax.swing.JTextField();
        MCSTextField = new javax.swing.JTextField();
        colorComboBox = new javax.swing.JComboBox<>();
        selectGrainsButton = new javax.swing.JButton();
        switchButton = new javax.swing.JButton();
        homogenousToggleButton = new javax.swing.JToggleButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        energyInsideTextField = new javax.swing.JTextField();
        energyOnEdgesTextField = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        microstructureMenu = new javax.swing.JMenu();
        importMenuItem = new javax.swing.JMenuItem();
        exportMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(700, 700));
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1MouseClicked(evt);
            }
        });
        jPanel1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jPanel1ComponentResized(evt);
            }
        });
        jPanel1.setLayout(new java.awt.CardLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 682, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 784, Short.MAX_VALUE)
        );

        jPanel1.add(jPanel2, "card2");

        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        createButton.setText("Create");
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createButtonActionPerformed(evt);
            }
        });

        addGrainsButton.setText("Add grains");
        addGrainsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addGrainsButtonActionPerformed(evt);
            }
        });

        numbersOfGrainsTextField.setText("30");

        jLabel1.setText("Numbers of grains");

        jLabel2.setText("Amount of inclusions");

        jLabel3.setText("Size of inclusions");

        jLabel4.setText("Type of inclusion");

        amountOfInclusionsTextField.setText("6");

        sizeOfInclusionsTextField.setText("5");

        typeOfInclusionComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "circular", "square" }));
        typeOfInclusionComboBox.setToolTipText("");
        typeOfInclusionComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                typeOfInclusionComboBoxItemStateChanged(evt);
            }
        });

        addInclusionsButton.setText("Add inclusions");
        addInclusionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addInclusionsButtonActionPerformed(evt);
            }
        });

        widthTextField.setText("200");

        jLabel5.setText("Width");

        jLabel6.setText("Height");

        heightTextField.setText("200");

        jLabel7.setText("Type of neighborhood");

        typeOfNeighborhoodComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "von Neumann", "Moore", "Moore 2" }));

        GBAllGrainsButton.setText("GB all grains");
        GBAllGrainsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GBAllGrainsButtonActionPerformed(evt);
            }
        });

        clearSpaceButton.setText("Clear space");
        clearSpaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearSpaceButtonActionPerformed(evt);
            }
        });

        perOfGBLabel.setText("% of GB");

        GBSingleGrainsButton.setText("GB single grains");
        GBSingleGrainsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GBSingleGrainsButtonActionPerformed(evt);
            }
        });

        generateMonteCarlo.setText("Generate MC");
        generateMonteCarlo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateMonteCarloActionPerformed(evt);
            }
        });

        startMonteCarlo.setText("Start MC");
        startMonteCarlo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startMonteCarloActionPerformed(evt);
            }
        });

        jLabel8.setText("N");

        jLabel9.setText("MCS");

        stateNumberTextField.setText("4");

        MCSTextField.setText("30");
        MCSTextField.setToolTipText("");

        colorComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        colorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorComboBoxActionPerformed(evt);
            }
        });

        selectGrainsButton.setText("Select grains");
        selectGrainsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectGrainsButtonActionPerformed(evt);
            }
        });

        switchButton.setText("Switch views");
        switchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchButtonActionPerformed(evt);
            }
        });

        homogenousToggleButton.setText("Homogenous");
        homogenousToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                homogenousToggleButtonActionPerformed(evt);
            }
        });

        jLabel10.setText("Energy inside");

        jLabel11.setText("Energy on edges");

        energyInsideTextField.setText("1");

        energyOnEdgesTextField.setText("9");

        fileMenu.setText("File");

        microstructureMenu.setText("Microstructure");

        importMenuItem.setText("Import");
        importMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importMenuItemActionPerformed(evt);
            }
        });
        microstructureMenu.add(importMenuItem);

        exportMenuItem.setText("Export");
        exportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportMenuItemActionPerformed(evt);
            }
        });
        microstructureMenu.add(exportMenuItem);

        fileMenu.add(microstructureMenu);

        jMenuBar1.add(fileMenu);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 683, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(startButton, javax.swing.GroupLayout.PREFERRED_SIZE, 358, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(typeOfNeighborhoodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 682, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(selectGrainsButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(colorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(GBSingleGrainsButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(perOfGBLabel)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(typeOfInclusionComboBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(numbersOfGrainsTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addGap(22, 22, 22)
                                        .addComponent(widthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel4)
                                        .addComponent(addInclusionsButton)
                                        .addComponent(jLabel3)
                                        .addComponent(jLabel2)
                                        .addComponent(addGrainsButton)
                                        .addComponent(jLabel1)
                                        .addComponent(createButton)
                                        .addComponent(GBAllGrainsButton)
                                        .addComponent(clearSpaceButton)
                                        .addComponent(generateMonteCarlo)
                                        .addComponent(startMonteCarlo)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(224, 224, 224)
                                        .addComponent(GBsizeLabel)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(sizeOfInclusionsTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(amountOfInclusionsTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                    .addComponent(jLabel6)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(heightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(jLabel9)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(MCSTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(jLabel8)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(stateNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(switchButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(homogenousToggleButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(energyOnEdgesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(energyInsideTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(widthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(heightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(32, 32, 32)
                        .addComponent(createButton)
                        .addGap(37, 37, 37)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(numbersOfGrainsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(addGrainsButton)
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(amountOfInclusionsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(sizeOfInclusionsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(typeOfInclusionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addGap(18, 18, 18)
                        .addComponent(addInclusionsButton)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(GBAllGrainsButton)
                            .addComponent(GBsizeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(GBSingleGrainsButton)
                            .addComponent(perOfGBLabel))
                        .addGap(28, 28, 28)
                        .addComponent(clearSpaceButton)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(generateMonteCarlo)
                            .addComponent(jLabel8)
                            .addComponent(stateNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(startMonteCarlo)
                            .addComponent(jLabel9)
                            .addComponent(MCSTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(colorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(selectGrainsButton))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(switchButton)
                            .addComponent(homogenousToggleButton))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(energyInsideTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(energyOnEdgesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(41, 41, 41)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(typeOfNeighborhoodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(startButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public void vonNeumann() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (matrix[i][j].id != -1) {
                    if (matrix[i][j].color == Color.BLACK || matrix[i][j].color == dualPhaseColor) {
                        matrix2[i][j] = matrix[i][j];
                    } else if (i > 0 && i < width - 1 && j > 0 && j < height - 1) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id == -1) {
                            matrix2[i][j - 1] = matrix[i][j];
                        }
                        if (matrix[i + 1][j].id == -1) {
                            matrix2[i + 1][j] = matrix[i][j];
                        }
                        if (matrix[i][j + 1].id == -1) {
                            matrix2[i][j + 1] = matrix[i][j];
                        }
                        if (matrix[i - 1][j].id == -1) {
                            matrix2[i - 1][j] = matrix[i][j];
                        }
                    } else if (i == 0 && i < width - 1 && j > 0 && j < height - 1) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id == -1) {
                            matrix2[i][j - 1] = matrix[i][j];
                        }
                        if (matrix[i + 1][j].id == -1) {
                            matrix2[i + 1][j] = matrix[i][j];
                        }
                        if (matrix[i][j + 1].id == -1) {
                            matrix2[i][j + 1] = matrix[i][j];
                        }
                    } else if (i > 0 && i < width - 1 && j == 0 && j < height - 1) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i + 1][j].id == -1) {
                            matrix2[i + 1][j] = matrix[i][j];
                        }
                        if (matrix[i][j + 1].id == -1) {
                            matrix2[i][j + 1] = matrix[i][j];
                        }
                        if (matrix[i - 1][j].id == -1) {
                            matrix2[i - 1][j] = matrix[i][j];
                        }
                    } else if (i > 0 && i == width - 1 && j > 0 && j < height - 1) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id == -1) {
                            matrix2[i][j - 1] = matrix[i][j];
                        }
                        if (matrix[i][j + 1].id == -1) {
                            matrix2[i][j + 1] = matrix[i][j];
                        }
                        if (matrix[i - 1][j].id == -1) {
                            matrix2[i - 1][j] = matrix[i][j];
                        }
                    } else if (i > 0 && i < width - 1 && j > 0 && j == height - 1) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id == -1) {
                            matrix2[i][j - 1] = matrix[i][j];
                        }
                        if (matrix[i + 1][j].id == -1) {
                            matrix2[i + 1][j] = matrix[i][j];
                        }
                        if (matrix[i - 1][j].id == -1) {
                            matrix2[i - 1][j] = matrix[i][j];
                        }
                    } else if (i == 0 && j == 0) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i + 1][j].id == -1) {
                            matrix2[i + 1][j] = matrix[i][j];
                        }
                        if (matrix[i][j + 1].id == -1) {
                            matrix2[i][j + 1] = matrix[i][j];
                        }
                    } else if (i == width - 1 && j == 0) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j + 1].id == -1) {
                            matrix2[i][j + 1] = matrix[i][j];
                        }
                        if (matrix[i - 1][j].id == -1) {
                            matrix2[i - 1][j] = matrix[i][j];
                        }
                    } else if (i == 0 && j == height - 1) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id == -1) {
                            matrix2[i][j - 1] = matrix[i][j];
                        }
                        if (matrix[i + 1][j].id == -1) {
                            matrix2[i + 1][j] = matrix[i][j];
                        }
                    } else if (i == width - 1 && j == height - 1) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id == -1) {
                            matrix2[i][j - 1] = matrix[i][j];
                        }
                        if (matrix[i - 1][j].id == -1) {
                            matrix2[i - 1][j] = matrix[i][j];
                        }
                    }
                }
                if (matrix[i][j].id == -1) {
                    if (i == 0 && j == 0) {
                        matrix2[i][j] = matrix[i + 1][j + 1];
                    }
                    if (i == width - 1 && j == 0) {
                        matrix2[i][j] = matrix[i - 1][j + 1];
                    }
                    if (i == 0 && j == height - 1) {
                        matrix2[i][j] = matrix[i + 1][j - 1];
                    }
                    if (i == width - 1 && j == height - 1) {
                        matrix2[i][j] = matrix[i - 1][j - 1];
                    }
                }
            }
        }
        change(matrix, matrix2, width, height);
    }

    public void moore() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (matrix[i][j].id != -1) {
                    if (matrix[i][j].color == Color.BLACK || matrix[i][j].color == dualPhaseColor) {
                        matrix2[i][j] = matrix[i][j];
                    } else if (i > 0 && i < width - 1 && j > 0 && j < height - 1) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id == -1) {
                            matrix2[i][j - 1] = matrix[i][j];
                        }
                        if (matrix[i + 1][j - 1].id == -1) {
                            matrix2[i + 1][j - 1] = matrix[i][j];
                        }
                        if (matrix[i + 1][j].id == -1) {
                            matrix2[i + 1][j] = matrix[i][j];
                        }
                        if (matrix[i + 1][j + 1].id == -1) {
                            matrix2[i + 1][j + 1] = matrix[i][j];
                        }
                        if (matrix[i][j + 1].id == -1) {
                            matrix2[i][j + 1] = matrix[i][j];
                        }
                        if (matrix[i - 1][j + 1].id == -1) {
                            matrix2[i - 1][j + 1] = matrix[i][j];
                        }
                        if (matrix[i - 1][j].id == -1) {
                            matrix2[i - 1][j] = matrix[i][j];
                        }
                        if (matrix[i - 1][j - 1].id == -1) {
                            matrix2[i - 1][j - 1] = matrix[i][j];
                        }
                    } else if (i == 0 && i < width - 1 && j > 0 && j < height - 1) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id == -1) {
                            matrix2[i][j - 1] = matrix[i][j];
                        }
                        if (matrix[i + 1][j - 1].id == -1) {
                            matrix2[i + 1][j - 1] = matrix[i][j];
                        }
                        if (matrix[i + 1][j].id == -1) {
                            matrix2[i + 1][j] = matrix[i][j];
                        }
                        if (matrix[i + 1][j + 1].id == -1) {
                            matrix2[i + 1][j + 1] = matrix[i][j];
                        }
                        if (matrix[i][j + 1].id == -1) {
                            matrix2[i][j + 1] = matrix[i][j];
                        }
                    } else if (i > 0 && i < width - 1 && j == 0 && j < height - 1) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i + 1][j].id == -1) {
                            matrix2[i + 1][j] = matrix[i][j];
                        }
                        if (matrix[i + 1][j + 1].id == -1) {
                            matrix2[i + 1][j + 1] = matrix[i][j];
                        }
                        if (matrix[i][j + 1].id == -1) {
                            matrix2[i][j + 1] = matrix[i][j];
                        }
                        if (matrix[i - 1][j + 1].id == -1) {
                            matrix2[i - 1][j + 1] = matrix[i][j];
                        }
                        if (matrix[i - 1][j].id == -1) {
                            matrix2[i - 1][j] = matrix[i][j];
                        }
                    } else if (i > 0 && i == width - 1 && j > 0 && j < height - 1) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id == -1) {
                            matrix2[i][j - 1] = matrix[i][j];
                        }
                        if (matrix[i - 1][j - 1].id == -1) {
                            matrix2[i - 1][j - 1] = matrix[i][j];
                        }
                        if (matrix[i][j + 1].id == -1) {
                            matrix2[i][j + 1] = matrix[i][j];
                        }
                        if (matrix[i - 1][j + 1].id == -1) {
                            matrix2[i - 1][j + 1] = matrix[i][j];
                        }
                        if (matrix[i - 1][j].id == -1) {
                            matrix2[i - 1][j] = matrix[i][j];
                        }
                    } else if (i > 0 && i < width - 1 && j > 0 && j == height - 1) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id == -1) {
                            matrix2[i][j - 1] = matrix[i][j];
                        }
                        if (matrix[i + 1][j - 1].id == -1) {
                            matrix2[i + 1][j - 1] = matrix[i][j];
                        }
                        if (matrix[i + 1][j].id == -1) {
                            matrix2[i + 1][j] = matrix[i][j];
                        }
                        if (matrix[i - 1][j].id == -1) {
                            matrix2[i - 1][j] = matrix[i][j];
                        }
                        if (matrix[i - 1][j - 1].id == -1) {
                            matrix2[i - 1][j - 1] = matrix[i][j];
                        }
                    } else if (i == 0 && j == 0) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i + 1][j].id == -1) {
                            matrix2[i + 1][j] = matrix[i][j];
                        }
                        if (matrix[i + 1][j + 1].id == -1) {
                            matrix2[i + 1][j + 1] = matrix[i][j];
                        }
                        if (matrix[i][j + 1].id == -1) {
                            matrix2[i][j + 1] = matrix[i][j];
                        }
                    } else if (i == width - 1 && j == 0) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j + 1].id == -1) {
                            matrix2[i][j + 1] = matrix[i][j];
                        }
                        if (matrix[i - 1][j + 1].id == -1) {
                            matrix2[i - 1][j + 1] = matrix[i][j];
                        }
                        if (matrix[i - 1][j].id == -1) {
                            matrix2[i - 1][j] = matrix[i][j];
                        }
                    } else if (i == 0 && j == height - 1) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id == -1) {
                            matrix2[i][j - 1] = matrix[i][j];
                        }
                        if (matrix[i + 1][j - 1].id == -1) {
                            matrix2[i + 1][j - 1] = matrix[i][j];
                        }
                        if (matrix[i + 1][j].id == -1) {
                            matrix2[i + 1][j] = matrix[i][j];
                        }
                    } else if (i == width - 1 && j == height - 1) {
                        matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id == -1) {
                            matrix2[i][j - 1] = matrix[i][j];
                        }
                        if (matrix[i - 1][j - 1].id == -1) {
                            matrix2[i - 1][j - 1] = matrix[i][j];
                        }
                        if (matrix[i - 1][j].id == -1) {
                            matrix2[i - 1][j] = matrix[i][j];
                        }
                    }
                }
                if (matrix[i][j].id == -1) {
                    if (i == 0 && j == 0) {
                        matrix2[i][j] = matrix[i + 1][j + 1];
                    }
                    if (i == width - 1 && j == 0) {
                        matrix2[i][j] = matrix[i - 1][j + 1];
                    }
                    if (i == 0 && j == height - 1) {
                        matrix2[i][j] = matrix[i + 1][j - 1];
                    }
                    if (i == width - 1 && j == height - 1) {
                        matrix2[i][j] = matrix[i - 1][j - 1];
                    }
                }
            }
        }
        change(matrix, matrix2, width, height);
    }

    public void moore2() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (matrix[i][j].color == Color.BLACK || matrix[i][j].color == dualPhaseColor) {
                    matrix2[i][j] = matrix[i][j];
                }
                if (matrix[i][j].id == -1) {
                    neighbours = new HashMap<>();
                    if (matrix[i][j].color == Color.BLACK) {
                        matrix2[i][j] = matrix[i][j];
                    } else if (i > 0 && i < width - 1 && j > 0 && j < height - 1) {
                        // matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != -1 && matrix[i][j - 1].id != -16777216) {
                            if (neighbours.get(matrix[i][j - 1].id) == null) {
                                neighbours.put(matrix[i][j - 1].id, 0);
                            }
                            neighbours.put(matrix[i][j - 1].id, neighbours.get(matrix[i][j - 1].id) + 1);
                        }
                        if (matrix[i + 1][j - 1].id != -1 && matrix[i + 1][j - 1].id != -16777216) {
                            if (neighbours.get(matrix[i + 1][j - 1].id) == null) {
                                neighbours.put(matrix[i + 1][j - 1].id, 0);
                            }
                            neighbours.put(matrix[i + 1][j - 1].id, neighbours.get(matrix[i + 1][j - 1].id) + 1);
                        }
                        if (matrix[i + 1][j].id != -1 && matrix[i + 1][j].id != -16777216) {
                            if (neighbours.get(matrix[i + 1][j].id) == null) {
                                neighbours.put(matrix[i + 1][j].id, 0);
                            }
                            neighbours.put(matrix[i + 1][j].id, neighbours.get(matrix[i + 1][j].id) + 1);
                        }
                        if (matrix[i + 1][j + 1].id != -1 && matrix[i + 1][j + 1].id != -16777216) {
                            if (neighbours.get(matrix[i + 1][j + 1].id) == null) {
                                neighbours.put(matrix[i + 1][j + 1].id, 0);
                            }
                            neighbours.put(matrix[i + 1][j + 1].id, neighbours.get(matrix[i + 1][j + 1].id) + 1);
                        }
                        if (matrix[i][j + 1].id != -1 && matrix[i][j + 1].id != -16777216) {
                            if (neighbours.get(matrix[i][j + 1].id) == null) {
                                neighbours.put(matrix[i][j + 1].id, 0);
                            }
                            neighbours.put(matrix[i][j + 1].id, neighbours.get(matrix[i][j + 1].id) + 1);
                        }
                        if (matrix[i - 1][j + 1].id != -1 && matrix[i - 1][j + 1].id != -16777216) {
                            if (neighbours.get(matrix[i - 1][j + 1].id) == null) {
                                neighbours.put(matrix[i - 1][j + 1].id, 0);
                            }
                            neighbours.put(matrix[i - 1][j + 1].id, neighbours.get(matrix[i - 1][j + 1].id) + 1);
                        }
                        if (matrix[i - 1][j].id != -1 && matrix[i - 1][j].id != -16777216) {
                            if (neighbours.get(matrix[i - 1][j].id) == null) {
                                neighbours.put(matrix[i - 1][j].id, 0);
                            }
                            neighbours.put(matrix[i - 1][j].id, neighbours.get(matrix[i - 1][j].id) + 1);
                        }
                        if (matrix[i - 1][j - 1].id != -1 && matrix[i - 1][j - 1].id != -16777216) {
                            if (neighbours.get(matrix[i - 1][j - 1].id) == null) {
                                neighbours.put(matrix[i - 1][j - 1].id, 0);
                            }
                            neighbours.put(matrix[i - 1][j - 1].id, neighbours.get(matrix[i - 1][j - 1].id) + 1);
                        }
                    } else if (i == 0 && i < width - 1 && j > 0 && j < height - 1) {
                        // matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != -1 && matrix[i][j - 1].id != -16777216) {
                            if (neighbours.get(matrix[i][j - 1].id) == null) {
                                neighbours.put(matrix[i][j - 1].id, 0);
                            }
                            neighbours.put(matrix[i][j - 1].id, neighbours.get(matrix[i][j - 1].id) + 1);
                        }
                        if (matrix[i + 1][j - 1].id != -1 && matrix[i + 1][j - 1].id != -16777216) {
                            if (neighbours.get(matrix[i + 1][j - 1].id) == null) {
                                neighbours.put(matrix[i + 1][j - 1].id, 0);
                            }
                            neighbours.put(matrix[i + 1][j - 1].id, neighbours.get(matrix[i + 1][j - 1].id) + 1);
                        }
                        if (matrix[i + 1][j].id != -1 && matrix[i + 1][j].id != -16777216) {
                            if (neighbours.get(matrix[i + 1][j].id) == null) {
                                neighbours.put(matrix[i + 1][j].id, 0);
                            }
                            neighbours.put(matrix[i + 1][j].id, neighbours.get(matrix[i + 1][j].id) + 1);
                        }
                        if (matrix[i + 1][j + 1].id != -1 && matrix[i + 1][j + 1].id != -16777216) {
                            if (neighbours.get(matrix[i + 1][j + 1].id) == null) {
                                neighbours.put(matrix[i + 1][j + 1].id, 0);
                            }
                            neighbours.put(matrix[i + 1][j + 1].id, neighbours.get(matrix[i + 1][j + 1].id) + 1);
                        }
                        if (matrix[i][j + 1].id != -1 && matrix[i][j + 1].id != -16777216) {
                            if (neighbours.get(matrix[i][j + 1].id) == null) {
                                neighbours.put(matrix[i][j + 1].id, 0);
                            }
                            neighbours.put(matrix[i][j + 1].id, neighbours.get(matrix[i][j + 1].id) + 1);
                        }
                    } else if (i > 0 && i < width - 1 && j == 0 && j < height - 1) {
                        //matrix2[i][j] = matrix[i][j];
                        if (matrix[i + 1][j].id != -1 && matrix[i + 1][j].id != -16777216) {
                            if (neighbours.get(matrix[i + 1][j].id) == null) {
                                neighbours.put(matrix[i + 1][j].id, 0);
                            }
                            neighbours.put(matrix[i + 1][j].id, neighbours.get(matrix[i + 1][j].id) + 1);
                        }
                        if (matrix[i + 1][j + 1].id != -1 && matrix[i + 1][j + 1].id != -16777216) {
                            if (neighbours.get(matrix[i + 1][j + 1].id) == null) {
                                neighbours.put(matrix[i + 1][j + 1].id, 0);
                            }
                            neighbours.put(matrix[i + 1][j + 1].id, neighbours.get(matrix[i + 1][j + 1].id) + 1);
                        }
                        if (matrix[i][j + 1].id != -1 && matrix[i][j + 1].id != -16777216) {
                            if (neighbours.get(matrix[i][j + 1].id) == null) {
                                neighbours.put(matrix[i][j + 1].id, 0);
                            }
                            neighbours.put(matrix[i][j + 1].id, neighbours.get(matrix[i][j + 1].id) + 1);
                        }
                        if (matrix[i - 1][j + 1].id != -1 && matrix[i - 1][j + 1].id != -16777216) {
                            if (neighbours.get(matrix[i - 1][j + 1].id) == null) {
                                neighbours.put(matrix[i - 1][j + 1].id, 0);
                            }
                            neighbours.put(matrix[i - 1][j + 1].id, neighbours.get(matrix[i - 1][j + 1].id) + 1);
                        }
                        if (matrix[i - 1][j].id != -1 && matrix[i - 1][j].id != -16777216) {
                            if (neighbours.get(matrix[i - 1][j].id) == null) {
                                neighbours.put(matrix[i - 1][j].id, 0);
                            }
                            neighbours.put(matrix[i - 1][j].id, neighbours.get(matrix[i - 1][j].id) + 1);
                        }
                    } else if (i > 0 && i == width - 1 && j > 0 && j < height - 1) {
                        //matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != -1 && matrix[i][j - 1].id != -16777216) {
                            if (neighbours.get(matrix[i][j - 1].id) == null) {
                                neighbours.put(matrix[i][j - 1].id, 0);
                            }
                            neighbours.put(matrix[i][j - 1].id, neighbours.get(matrix[i][j - 1].id) + 1);
                        }
                        if (matrix[i - 1][j - 1].id != -1 && matrix[i - 1][j - 1].id != -16777216) {
                            if (neighbours.get(matrix[i - 1][j - 1].id) == null) {
                                neighbours.put(matrix[i - 1][j - 1].id, 0);
                            }
                            neighbours.put(matrix[i - 1][j - 1].id, neighbours.get(matrix[i - 1][j - 1].id) + 1);
                        }
                        if (matrix[i][j + 1].id != -1 && matrix[i][j + 1].id != -16777216) {
                            if (neighbours.get(matrix[i][j + 1].id) == null) {
                                neighbours.put(matrix[i][j + 1].id, 0);
                            }
                            neighbours.put(matrix[i][j + 1].id, neighbours.get(matrix[i][j + 1].id) + 1);
                        }
                        if (matrix[i - 1][j + 1].id != -1 && matrix[i - 1][j + 1].id != -16777216) {
                            if (neighbours.get(matrix[i - 1][j + 1].id) == null) {
                                neighbours.put(matrix[i - 1][j + 1].id, 0);
                            }
                            neighbours.put(matrix[i - 1][j + 1].id, neighbours.get(matrix[i - 1][j + 1].id) + 1);
                        }
                        if (matrix[i - 1][j].id != -1 && matrix[i - 1][j].id != -16777216) {
                            if (neighbours.get(matrix[i - 1][j].id) == null) {
                                neighbours.put(matrix[i - 1][j].id, 0);
                            }
                            neighbours.put(matrix[i - 1][j].id, neighbours.get(matrix[i - 1][j].id) + 1);
                        }
                    } else if (i > 0 && i < width - 1 && j > 0 && j == height - 1) {
                        // matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != -1 && matrix[i][j - 1].id != -16777216) {
                            if (neighbours.get(matrix[i][j - 1].id) == null) {
                                neighbours.put(matrix[i][j - 1].id, 0);
                            }
                            neighbours.put(matrix[i][j - 1].id, neighbours.get(matrix[i][j - 1].id) + 1);
                        }
                        if (matrix[i + 1][j - 1].id != -1 && matrix[i + 1][j - 1].id != -16777216) {
                            if (neighbours.get(matrix[i + 1][j - 1].id) == null) {
                                neighbours.put(matrix[i + 1][j - 1].id, 0);
                            }
                            neighbours.put(matrix[i + 1][j - 1].id, neighbours.get(matrix[i + 1][j - 1].id) + 1);
                        }
                        if (matrix[i + 1][j].id != -1 && matrix[i + 1][j].id != -16777216) {
                            if (neighbours.get(matrix[i + 1][j].id) == null) {
                                neighbours.put(matrix[i + 1][j].id, 0);
                            }
                            neighbours.put(matrix[i + 1][j].id, neighbours.get(matrix[i + 1][j].id) + 1);
                        }
                        if (matrix[i - 1][j].id != -1 && matrix[i - 1][j].id != -16777216) {
                            if (neighbours.get(matrix[i - 1][j].id) == null) {
                                neighbours.put(matrix[i - 1][j].id, 0);
                            }
                            neighbours.put(matrix[i - 1][j].id, neighbours.get(matrix[i - 1][j].id) + 1);
                        }
                        if (matrix[i - 1][j - 1].id != -1 && matrix[i - 1][j - 1].id != -16777216) {
                            if (neighbours.get(matrix[i - 1][j - 1].id) == null) {
                                neighbours.put(matrix[i - 1][j - 1].id, 0);
                            }
                            neighbours.put(matrix[i - 1][j - 1].id, neighbours.get(matrix[i - 1][j - 1].id) + 1);
                        }
                    } else if (i == 0 && j == 0) {
                        // matrix2[i][j] = matrix[i][j];
                        if (matrix[i + 1][j].id != -1 && matrix[i + 1][j].id != -16777216) {
                            if (neighbours.get(matrix[i + 1][j].id) == null) {
                                neighbours.put(matrix[i + 1][j].id, 0);
                            }
                            neighbours.put(matrix[i + 1][j].id, neighbours.get(matrix[i + 1][j].id) + 1);
                        }
                        if (matrix[i + 1][j + 1].id != -1 && matrix[i + 1][j + 1].id != -16777216) {
                            if (neighbours.get(matrix[i + 1][j + 1].id) == null) {
                                neighbours.put(matrix[i + 1][j + 1].id, 0);
                            }
                            neighbours.put(matrix[i + 1][j + 1].id, neighbours.get(matrix[i + 1][j + 1].id) + 1);
                        }
                        if (matrix[i][j + 1].id != -1 && matrix[i][j + 1].id != -16777216) {
                            if (neighbours.get(matrix[i][j + 1].id) == null) {
                                neighbours.put(matrix[i][j + 1].id, 0);
                            }
                            neighbours.put(matrix[i][j + 1].id, neighbours.get(matrix[i][j + 1].id) + 1);
                        }
                    } else if (i == width - 1 && j == 0) {
                        //matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j + 1].id != -1 && matrix[i][j + 1].id != -16777216) {
                            if (neighbours.get(matrix[i][j + 1].id) == null) {
                                neighbours.put(matrix[i][j + 1].id, 0);
                            }
                            neighbours.put(matrix[i][j + 1].id, neighbours.get(matrix[i][j + 1].id) + 1);
                        }
                        if (matrix[i - 1][j + 1].id != -1 && matrix[i - 1][j + 1].id != -16777216) {
                            if (neighbours.get(matrix[i - 1][j + 1].id) == null) {
                                neighbours.put(matrix[i - 1][j + 1].id, 0);
                            }
                            neighbours.put(matrix[i - 1][j + 1].id, neighbours.get(matrix[i - 1][j + 1].id) + 1);
                        }
                        if (matrix[i - 1][j].id != -1 && matrix[i - 1][j].id != -16777216) {
                            if (neighbours.get(matrix[i - 1][j].id) == null) {
                                neighbours.put(matrix[i - 1][j].id, 0);
                            }
                            neighbours.put(matrix[i - 1][j].id, neighbours.get(matrix[i - 1][j].id) + 1);
                        }
                    } else if (i == 0 && j == height - 1) {
                        //matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != -1 && matrix[i][j - 1].id != -16777216) {
                            if (neighbours.get(matrix[i][j - 1].id) == null) {
                                neighbours.put(matrix[i][j - 1].id, 0);
                            }
                            neighbours.put(matrix[i][j - 1].id, neighbours.get(matrix[i][j - 1].id) + 1);
                        }
                        if (matrix[i + 1][j - 1].id != -1 && matrix[i + 1][j - 1].id != -16777216) {
                            if (neighbours.get(matrix[i + 1][j - 1].id) == null) {
                                neighbours.put(matrix[i + 1][j - 1].id, 0);
                            }
                            neighbours.put(matrix[i + 1][j - 1].id, neighbours.get(matrix[i + 1][j - 1].id) + 1);
                        }
                        if (matrix[i + 1][j].id != -1 && matrix[i + 1][j].id != -16777216) {
                            if (neighbours.get(matrix[i + 1][j].id) == null) {
                                neighbours.put(matrix[i + 1][j].id, 0);
                            }
                            neighbours.put(matrix[i + 1][j].id, neighbours.get(matrix[i + 1][j].id) + 1);
                        }
                    } else if (i == width - 1 && j == height - 1) {
                        // matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != -1 && matrix[i][j - 1].id != -16777216) {
                            if (neighbours.get(matrix[i][j - 1].id) == null) {
                                neighbours.put(matrix[i][j - 1].id, 0);
                            }
                            neighbours.put(matrix[i][j - 1].id, neighbours.get(matrix[i][j - 1].id) + 1);
                        }
                        if (matrix[i - 1][j - 1].id != -1 && matrix[i - 1][j - 1].id != -16777216) {
                            if (neighbours.get(matrix[i - 1][j - 1].id) == null) {
                                neighbours.put(matrix[i - 1][j - 1].id, 0);
                            }
                            neighbours.put(matrix[i - 1][j - 1].id, neighbours.get(matrix[i - 1][j - 1].id) + 1);
                        }
                        if (matrix[i - 1][j].id != -1 && matrix[i - 1][j].id != -16777216) {
                            if (neighbours.get(matrix[i - 1][j].id) == null) {
                                neighbours.put(matrix[i - 1][j].id, 0);
                            }
                            neighbours.put(matrix[i - 1][j].id, neighbours.get(matrix[i - 1][j].id) + 1);
                        }
                    }

                    if (!neighbours.isEmpty()) {
                        //if (neighbours.get(matrix[i][j].id) != null && neighbours.get(matrix[i][j].id) == -16777216) neighbours.remove(matrix[i][j].id);
                        mostFrequent = new ArrayList<>();
                        Map.Entry<Integer, Integer> maxEntry = null;

                        for (Map.Entry<Integer, Integer> entry : neighbours.entrySet()) {
                            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) >= 0) {
                                maxEntry = entry;
                                mostFrequent.add(maxEntry);

                            }
                        }
                        if (!mostFrequent.isEmpty()) {
                            Random random = new Random();

                            int temp = Math.abs(random.nextInt()) % mostFrequent.size();
                            matrix2[i][j].id = mostFrequent.get(temp).getKey();
                            matrix2[i][j].color = new Color(matrix2[i][j].id);
                        }
                    }

                }

                /* if (matrix[i][j].id == -1) {
                    if (i == 0 && j == 0) {
                        matrix2[i][j] = matrix[i + 1][j + 1];
                    }
                    if (i == width - 1 && j == 0) {
                        matrix2[i][j] = matrix[i - 1][j + 1];
                    }
                    if (i == 0 && j == height - 1) {
                        matrix2[i][j] = matrix[i + 1][j - 1];
                    }
                    if (i == width - 1 && j == height - 1) {
                        matrix2[i][j] = matrix[i - 1][j - 1];
                    }
                }*/
            }
        }
        change(matrix, matrix2, width, height);

    }

    public boolean isFullMatrix() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (matrix[i][j].id == -1) {
                    return false;
                }
            }
        }
        play = !play;
        startButton.setText("Finished");
        startButton.setEnabled(false);
        addGrainsButton.setEnabled(false);
        addInclusionsButton.setEnabled(true);
        GBAllGrainsButton.setEnabled(true);
        GBSingleGrainsButton.setEnabled(true);
        return true;
    }

    public void change(Cell[][] a, Cell[][] b, int width, int height) {

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                a[i][j].color = b[i][j].color;
                a[i][j].id = b[i][j].id;
            }
        }
    }

    public void monteCarlo() {
        done = 0;

        Random random = new Random();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                matrix[i][j].visited = false;
            }
        }
        while (done < width * height) {

            randomX = Math.abs(random.nextInt()) % width;
            randomY = Math.abs(random.nextInt()) % height;
            if (matrix[randomX][randomY].visited == false) {
                if (matrix[randomX][randomY].color != Color.BLACK && matrix[randomX][randomY].color != dualPhaseColor) {
                    energyBefore = calculateEnergy(randomX, randomY);
                    //cellBefore = matrix[randomX][randomY]; 
                    colorBefore = matrix[randomX][randomY].color;
                    randomColor = Math.abs(random.nextInt()) % stateNumber;
                    matrix[randomX][randomY].color = stateNumberList.get(randomColor);
                    matrix[randomX][randomY].id = Cell.idColor(matrix[randomX][randomY].color);
                    //cellAfter = matrix[randomX][randomY];
                    energyAfter = calculateEnergy(randomX, randomY);

                    /*if ((energyAfter - energyBefore) <= 0) {
                    matrix[randomX][randomY] = cellAfter;
                } else */
                    if ((energyAfter - energyBefore) > 0) {
                        matrix[randomX][randomY].color = colorBefore;
                        matrix[randomX][randomY].id = Cell.idColor(matrix[randomX][randomY].color);
                    }
                    matrix[randomX][randomY].visited = true;
                }
                done++;

            }
        }
    }

    public int calculateEnergy(int i, int j) {
        energy = 0;

        if (i > 0 && i < width - 1 && j > 0 && j < height - 1) {
            if (matrix[i][j - 1].color != matrix[i][j].color && matrix[i][j - 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i + 1][j - 1].color != matrix[i][j].color && matrix[i + 1][j - 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i + 1][j].color != matrix[i][j].color && matrix[i + 1][j].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i + 1][j + 1].color != matrix[i][j].color && matrix[i + 1][j + 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i][j + 1].color != matrix[i][j].color && matrix[i][j + 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i - 1][j + 1].color != matrix[i][j].color && matrix[i - 1][j + 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i - 1][j].color != matrix[i][j].color && matrix[i - 1][j].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i - 1][j - 1].color != matrix[i][j].color && matrix[i - 1][j - 1].color != dualPhaseColor) {
                energy++;
            }
        } else if (i == 0 && i < width - 1 && j > 0 && j < height - 1) {
            if (matrix[i][j - 1].color != matrix[i][j].color && matrix[i][j - 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i + 1][j - 1].color != matrix[i][j].color && matrix[i + 1][j - 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i + 1][j].color != matrix[i][j].color && matrix[i + 1][j].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i + 1][j + 1].color != matrix[i][j].color && matrix[i + 1][j + 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i][j + 1].color != matrix[i][j].color && matrix[i][j + 1].color != dualPhaseColor) {
                energy++;
            }
        } else if (i > 0 && i < width - 1 && j == 0 && j < height - 1) {
            if (matrix[i + 1][j].color != matrix[i][j].color && matrix[i + 1][j].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i + 1][j + 1].color != matrix[i][j].color && matrix[i + 1][j + 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i][j + 1].color != matrix[i][j].color && matrix[i][j + 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i - 1][j + 1].color != matrix[i][j].color && matrix[i - 1][j + 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i - 1][j].color != matrix[i][j].color && matrix[i - 1][j].color != dualPhaseColor) {
                energy++;
            }
        } else if (i > 0 && i == width - 1 && j > 0 && j < height - 1) {
            if (matrix[i][j - 1].color != matrix[i][j].color && matrix[i][j - 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i - 1][j - 1].color != matrix[i][j].color && matrix[i - 1][j - 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i][j + 1].color != matrix[i][j].color && matrix[i][j + 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i - 1][j + 1].color != matrix[i][j].color && matrix[i - 1][j + 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i - 1][j].color != matrix[i][j].color && matrix[i - 1][j].color != dualPhaseColor) {
                energy++;
            }
        } else if (i > 0 && i < width - 1 && j > 0 && j == height - 1) {
            if (matrix[i][j - 1].color != matrix[i][j].color && matrix[i][j - 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i + 1][j - 1].color != matrix[i][j].color && matrix[i + 1][j - 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i + 1][j].color != matrix[i][j].color && matrix[i + 1][j].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i - 1][j].color != matrix[i][j].color && matrix[i - 1][j].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i - 1][j - 1].color != matrix[i][j].color && matrix[i - 1][j - 1].color != dualPhaseColor) {
                energy++;
            }
        } else if (i == 0 && j == 0) {
            if (matrix[i + 1][j].color != matrix[i][j].color && matrix[i + 1][j].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i + 1][j + 1].color != matrix[i][j].color && matrix[i + 1][j + 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i][j + 1].color != matrix[i][j].color && matrix[i][j + 1].color != dualPhaseColor) {
                energy++;
            }
        } else if (i == width - 1 && j == 0) {
            if (matrix[i][j + 1].color != matrix[i][j].color && matrix[i][j + 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i - 1][j + 1].color != matrix[i][j].color && matrix[i - 1][j + 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i - 1][j].color != matrix[i][j].color && matrix[i - 1][j].color != dualPhaseColor) {
                energy++;
            }
        } else if (i == 0 && j == height - 1) {
            if (matrix[i][j - 1].color != matrix[i][j].color && matrix[i][j - 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i + 1][j - 1].color != matrix[i][j].color && matrix[i + 1][j - 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i + 1][j].color != matrix[i][j].color && matrix[i + 1][j].color != dualPhaseColor) {
                energy++;
            }
        } else if (i == width - 1 && j == height - 1) {
            if (matrix[i][j - 1].color != matrix[i][j].color && matrix[i][j - 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i - 1][j - 1].color != matrix[i][j].color && matrix[i - 1][j - 1].color != dualPhaseColor) {
                energy++;
            }
            if (matrix[i - 1][j].color != matrix[i][j].color && matrix[i - 1][j].color != dualPhaseColor) {
                energy++;
            }
        }
        return energy;
    }

    public void energyDistribution(Cell[][] a) {
        matrixED = new Cell[width][height];
        if (homogenousToggleButton.isSelected() == true) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    matrixED[i][j] = new Cell();
                    matrixED[i][j].color = Cell.energyColor(Integer.parseInt(energyInsideTextField.getText()));
                }
            }
        } else if (homogenousToggleButton.isSelected() == false) {
            Cell energyOnEdges = new Cell();
            energyOnEdges.color = Cell.energyColor(Integer.parseInt(energyOnEdgesTextField.getText()));
            energyOnEdges.id = Cell.idColor(energyOnEdges.color);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    matrixED[i][j] = new Cell();
                    matrixED[i][j].color = Cell.energyColor(Integer.parseInt(energyInsideTextField.getText()));
                    if (i > 0 && i < width - 1 && j > 0 && j < height - 1) {
                        if (matrix[i][j - 1].id != matrix[i][j].id) {
                            matrixED[i][j - 1] = energyOnEdges;
                        }
                        if (matrix[i + 1][j - 1].id != matrix[i][j].id) {
                            matrixED[i + 1][j - 1] = energyOnEdges;
                        }
                        if (matrix[i + 1][j].id != matrix[i][j].id) {
                            matrixED[i + 1][j] = energyOnEdges;
                        }
                        if (matrix[i + 1][j + 1].id != matrix[i][j].id) {
                            matrixED[i + 1][j + 1] = energyOnEdges;
                        }
                        if (matrix[i][j + 1].id != matrix[i][j].id) {
                            matrixED[i][j + 1] = energyOnEdges;
                        }
                        if (matrix[i - 1][j + 1].id != matrix[i][j].id) {
                            matrixED[i - 1][j + 1] = energyOnEdges;
                        }
                        if (matrix[i - 1][j].id != matrix[i][j].id) {
                            matrixED[i - 1][j] = energyOnEdges;
                        }
                        if (matrix[i - 1][j - 1].id != matrix[i][j].id) {
                            matrixED[i - 1][j - 1] = energyOnEdges;
                        }
                    } else if (i == 0 && i < width - 1 && j > 0 && j < height - 1) {
                        //matrixED[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != matrix[i][j].id) {
                            matrixED[i][j - 1] = energyOnEdges;
                        }
                        if (matrix[i + 1][j - 1].id != matrix[i][j].id) {
                            matrixED[i + 1][j - 1] = energyOnEdges;
                        }
                        if (matrix[i + 1][j].id != matrix[i][j].id) {
                            matrixED[i + 1][j] = energyOnEdges;
                        }
                        if (matrix[i + 1][j + 1].id != matrix[i][j].id) {
                            matrixED[i + 1][j + 1] = energyOnEdges;
                        }
                        if (matrix[i][j + 1].id != matrix[i][j].id) {
                            matrixED[i][j + 1] = energyOnEdges;
                        }
                    } else if (i > 0 && i < width - 1 && j == 0 && j < height - 1) {
                        //matrixED[i][j] = matrix[i][j];
                        if (matrix[i + 1][j].id != matrix[i][j].id) {
                            matrixED[i + 1][j] = energyOnEdges;
                        }
                        if (matrix[i + 1][j + 1].id != matrix[i][j].id) {
                            matrixED[i + 1][j + 1] = energyOnEdges;
                        }
                        if (matrix[i][j + 1].id != matrix[i][j].id) {
                            matrixED[i][j + 1] = energyOnEdges;
                        }
                        if (matrix[i - 1][j + 1].id != matrix[i][j].id) {
                            matrixED[i - 1][j + 1] = energyOnEdges;
                        }
                        if (matrix[i - 1][j].id != matrix[i][j].id) {
                            matrixED[i - 1][j] = energyOnEdges;
                        }
                    } else if (i > 0 && i == width - 1 && j > 0 && j < height - 1) {
                        //matrixED[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != matrix[i][j].id) {
                            matrixED[i][j - 1] = energyOnEdges;
                        }
                        if (matrix[i - 1][j - 1].id != matrix[i][j].id) {
                            matrixED[i - 1][j - 1] = energyOnEdges;
                        }
                        if (matrix[i][j + 1].id != matrix[i][j].id) {
                            matrixED[i][j + 1] = energyOnEdges;
                        }
                        if (matrix[i - 1][j + 1].id != matrix[i][j].id) {
                            matrixED[i - 1][j + 1] = energyOnEdges;
                        }
                        if (matrix[i - 1][j].id != matrix[i][j].id) {
                            matrixED[i - 1][j] = energyOnEdges;
                        }
                    } else if (i > 0 && i < width - 1 && j > 0 && j == height - 1) {
                        //matrixED[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != matrix[i][j].id) {
                            matrixED[i][j - 1] = energyOnEdges;
                        }
                        if (matrix[i + 1][j - 1].id != matrix[i][j].id) {
                            matrixED[i + 1][j - 1] = energyOnEdges;
                        }
                        if (matrix[i + 1][j].id != matrix[i][j].id) {
                            matrixED[i + 1][j] = energyOnEdges;
                        }
                        if (matrix[i - 1][j].id != matrix[i][j].id) {
                            matrixED[i - 1][j] = energyOnEdges;
                        }
                        if (matrix[i - 1][j - 1].id != matrix[i][j].id) {
                            matrixED[i - 1][j - 1] = energyOnEdges;
                        }
                    } else if (i == 0 && j == 0) {
                        //matrixED[i][j] = matrix[i][j];
                        if (matrix[i + 1][j].id != matrix[i][j].id) {
                            matrixED[i + 1][j] = energyOnEdges;
                        }
                        if (matrix[i + 1][j + 1].id != matrix[i][j].id) {
                            matrixED[i + 1][j + 1] = energyOnEdges;
                        }
                        if (matrix[i][j + 1].id != matrix[i][j].id) {
                            matrixED[i][j + 1] = energyOnEdges;
                        }
                    } else if (i == width - 1 && j == 0) {
                        //matrixED[i][j] = matrix[i][j];
                        if (matrix[i][j + 1].id != matrix[i][j].id) {
                            matrixED[i][j + 1] = energyOnEdges;
                        }
                        if (matrix[i - 1][j + 1].id != matrix[i][j].id) {
                            matrixED[i - 1][j + 1] = energyOnEdges;
                        }
                        if (matrix[i - 1][j].id != matrix[i][j].id) {
                            matrixED[i - 1][j] = energyOnEdges;
                        }
                    } else if (i == 0 && j == height - 1) {
                        //matrixED[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != matrix[i][j].id) {
                            matrixED[i][j - 1] = energyOnEdges;
                        }
                        if (matrix[i + 1][j - 1].id != matrix[i][j].id) {
                            matrixED[i + 1][j - 1] = energyOnEdges;
                        }
                        if (matrix[i + 1][j].id != matrix[i][j].id) {
                            matrixED[i + 1][j] = energyOnEdges;
                        }
                    } else if (i == width - 1 && j == height - 1) {
                        //matrixED[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != matrix[i][j].id) {
                            matrixED[i][j - 1] = energyOnEdges;
                        }
                        if (matrix[i - 1][j - 1].id != matrix[i][j].id) {
                            matrixED[i - 1][j - 1] = energyOnEdges;
                        }
                        if (matrix[i - 1][j].id != matrix[i][j].id) {
                            matrixED[i - 1][j] = energyOnEdges;
                        }
                    }
                }
            }
        }
    }
    private void jPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseClicked

        int i = width * evt.getX() / jPanel1.getWidth();
        int j = height * evt.getY() / jPanel1.getHeight();

        if (matrix[i][j].id == -1) {
            matrix[i][j].color = Cell.randomColor();
            matrix[i][j].id = Cell.idColor(matrix[i][j].color);
            stateNumberList.add(matrix[i][j].color);

        } else if (matrix[i][j].id != -1) {
            matrix[i][j].id = -1;
            stateNumberList.remove(matrix[i][j].color);
        }
        stateNumberList2 = stateNumberList;
        colorComboBox.setModel(new DefaultComboBoxModel(stateNumberList2.toArray()));
        refresh();
    }//GEN-LAST:event_jPanel1MouseClicked

    private void jPanel1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel1ComponentResized

        //offScrImg = createImage(jPanel1.getWidth(), jPanel1.getHeight());
        //offScrGraph = offScrImg.getGraphics();
        offScrImg = new BufferedImage(jPanel1.getWidth(), jPanel1.getHeight(), BufferedImage.TYPE_INT_RGB);
        offScrGraph = offScrImg.createGraphics();
        offScrGraph.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY));
        refresh();
    }//GEN-LAST:event_jPanel1ComponentResized

    private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed

        width = Integer.parseInt(widthTextField.getText());
        height = Integer.parseInt(heightTextField.getText());
        matrix = new Cell[width][height];
        matrix2 = new Cell[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                matrix[i][j] = new Cell();
                matrix2[i][j] = new Cell();
            }
        }
        refresh();
        startButton.setText("Start");
        startButton.setEnabled(true);
        addGrainsButton.setEnabled(true);
        addInclusionsButton.setEnabled(true);
        GBAllGrainsButton.setEnabled(false);
        GBSingleGrainsButton.setEnabled(false);
        clearSpaceButton.setEnabled(false);
        jProgressBar1.setValue(0);
        jProgressBar1.setString(0 + "% Complete");

        stateNumberList2.clear();
        colorComboBox.removeAllItems();
    }//GEN-LAST:event_createButtonActionPerformed

    private void addGrainsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addGrainsButtonActionPerformed

        Random random = new Random();
        randomX = 0;
        randomY = 0;
        grains = Integer.parseInt(numbersOfGrainsTextField.getText());
        while (grains > 0) {
            randomX = Math.abs(random.nextInt()) % width;
            randomY = Math.abs(random.nextInt()) % height;

            if (matrix[randomX][randomY].id == -1) {
                matrix[randomX][randomY].color = Cell.randomColor();
                matrix[randomX][randomY].id = Cell.idColor(matrix[randomX][randomY].color);
                stateNumberList.add(matrix[randomX][randomY].color);
                grains--;
            }

        }
        stateNumberList2 = stateNumberList;
        colorComboBox.setModel(new DefaultComboBoxModel(stateNumberList2.toArray()));
        refresh();
    }//GEN-LAST:event_addGrainsButtonActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed

        play = !play;
        if (play) {
            startButton.setText("Pause");
            addInclusionsButton.setEnabled(false);

        } else {
            startButton.setText("Resume");
        }
    }//GEN-LAST:event_startButtonActionPerformed

    private void exportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportMenuItemActionPerformed
        // TODO add your handling code here:
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            boolean isTxt = file.getName().endsWith(".txt");
            if (isTxt) {
                try {
                    PrintWriter printWriter = new PrintWriter(new FileWriter(file));
                    //printWriter.println(width + " " + height);
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            printWriter.println(i + " " + j + " " + matrix[i][j].id);
                        }
                    }
                    printWriter.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            } else {
                try {
                    ImageIO.write(offScrImg, "png", file);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(MultiscaleModellingFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_exportMenuItemActionPerformed

    private void importMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importMenuItemActionPerformed
        // TODO add your handling code here:
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            boolean isTxt = file.getName().endsWith(".txt");
            if (isTxt) {
                BufferedReader bufferReader = null;
                try {
                    bufferReader = new BufferedReader(new FileReader(file));
                    String line = null;
                    String splited = null;
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            // f0.println(i + " " + j + " " + matrix[i][j].id);
                            line = bufferReader.readLine();
                            splited = line;
                            String[] splitedArray = null;
                            splitedArray = splited.split(" ");
                            matrix[i][j].id = Integer.parseInt(splitedArray[2]);
                            matrix[i][j].color = new Color(matrix[i][j].id);
                            if (matrix[i][j].id == -16777216) {
                                matrix[i][j].color = Color.BLACK;
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (bufferReader != null) {
                            bufferReader.close();
                            refresh();
                        }
                    } catch (IOException e) {
                    }
                }
            } else {
                try {
                    offScrImg = ImageIO.read(file);
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {

                            matrix[i][j].id = offScrImg.getRGB(i * (jPanel1.getWidth() / width), j * (jPanel1.getHeight() / height));
                            matrix[i][j].color = new Color(matrix[i][j].id);
                            if (matrix[i][j].id == -16777216) {
                                matrix[i][j].color = Color.BLACK;
                            }
                        }
                    }
                    refresh();
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(MultiscaleModellingFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_importMenuItemActionPerformed

    private void addInclusionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addInclusionsButtonActionPerformed
        // TODO add your handling code here:

        Random random = new Random();
        randomX = 0;
        randomY = 0;
        inclusions = Integer.parseInt(amountOfInclusionsTextField.getText());
        sizeOfInclusions = Integer.parseInt(sizeOfInclusionsTextField.getText());
        if (startButton.getText().equals("Start")) {
            if (typeOfInclusionComboBox.getSelectedItem().toString().equals("circular")) {
                while (inclusions > 0) {
                    randomX = Math.abs(random.nextInt()) % width;
                    randomY = Math.abs(random.nextInt()) % height;

                    if (randomX > sizeOfInclusions && randomX < width - sizeOfInclusions
                            && randomY > sizeOfInclusions && randomY < height - sizeOfInclusions) {
                        if (matrix[randomX + sizeOfInclusions][randomY + sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX + sizeOfInclusions][randomY].color != Color.BLACK
                                && matrix[randomX][randomY + sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX + sizeOfInclusions][randomY - sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX - sizeOfInclusions][randomY + sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX - sizeOfInclusions][randomY].color != Color.BLACK
                                && matrix[randomX][randomY - sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX - sizeOfInclusions][randomY - sizeOfInclusions].color != Color.BLACK) {
                            int x = sizeOfInclusions;
                            int y = 0;
                            int xChange = 1 - (sizeOfInclusions << 1);
                            int yChange = 0;
                            int radiusError = 0;

                            while (x >= y) {
                                for (int i = randomX - x; i <= randomX + x; i++) {
                                    matrix[i][randomY + y].color = Color.BLACK;
                                    matrix[i][randomY + y].id = Cell.idColor(matrix[i][randomY + y].color);
                                    matrix[i][randomY - y].color = Color.BLACK;
                                    matrix[i][randomY - y].id = Cell.idColor(matrix[i][randomY - y].color);
                                }
                                for (int i = randomX - y; i <= randomX + y; i++) {
                                    matrix[i][randomY + x].color = Color.BLACK;
                                    matrix[i][randomY + x].id = Cell.idColor(matrix[i][randomY + x].color);
                                    matrix[i][randomY - x].color = Color.BLACK;
                                    matrix[i][randomY - x].id = Cell.idColor(matrix[i][randomY - x].color);
                                }

                                y++;
                                radiusError += yChange;
                                yChange += 2;
                                if (((radiusError << 1) + xChange) > 0) {
                                    x--;
                                    radiusError += xChange;
                                    xChange += 2;
                                }
                            }
                            inclusions--;
                        }
                    }
                }
            }
            if (typeOfInclusionComboBox.getSelectedItem().toString().equals("square")) {
                while (inclusions > 0) {
                    randomX = Math.abs(random.nextInt()) % width;
                    randomY = Math.abs(random.nextInt()) % height;

                    if (randomX > sizeOfInclusions && randomX < width - sizeOfInclusions
                            && randomY > sizeOfInclusions && randomY < height - sizeOfInclusions) {
                        if (matrix[randomX + sizeOfInclusions][randomY + sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX + sizeOfInclusions][randomY].color != Color.BLACK
                                && matrix[randomX][randomY + sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX + sizeOfInclusions][randomY - sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX - sizeOfInclusions][randomY + sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX - sizeOfInclusions][randomY].color != Color.BLACK
                                && matrix[randomX][randomY - sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX - sizeOfInclusions][randomY - sizeOfInclusions].color != Color.BLACK) {
                            int a = (int) (sizeOfInclusions / Math.sqrt(2));
                            for (int i = randomX - (a / 2); i < randomX + (a / 2); i++) {
                                for (int j = randomY - (a / 2); j < randomY + (a / 2); j++) {
                                    matrix[i][j].color = Color.BLACK;
                                    matrix[i][j].id = Cell.idColor(matrix[i][j].color);
                                }
                            }
                            inclusions--;
                        }
                    }
                }
            }
            refresh();
        } else if (startButton.getText().equals("Finished")) {
            if (typeOfInclusionComboBox.getSelectedItem().toString().equals("circular")) {
                while (inclusions > 0) {
                    randomX = Math.abs(random.nextInt()) % width;
                    randomY = Math.abs(random.nextInt()) % height;

                    if (randomX > sizeOfInclusions && randomX < width - sizeOfInclusions
                            && randomY > sizeOfInclusions && randomY < height - sizeOfInclusions) {
                        if ((matrix[randomX][randomY].id != matrix[randomX + 1][randomY + 1].id
                                || matrix[randomX][randomY].id != matrix[randomX + 1][randomY].id
                                || matrix[randomX][randomY].id != matrix[randomX][randomY + 1].id
                                || matrix[randomX][randomY].id != matrix[randomX + 1][randomY - 1].id
                                || matrix[randomX][randomY].id != matrix[randomX - 1][randomY + 1].id
                                || matrix[randomX][randomY].id != matrix[randomX - 1][randomY].id
                                || matrix[randomX][randomY].id != matrix[randomX][randomY - 1].id
                                || matrix[randomX][randomY].id != matrix[randomX - 1][randomY - 1].id)
                                && matrix[randomX + sizeOfInclusions][randomY + sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX + sizeOfInclusions][randomY].color != Color.BLACK
                                && matrix[randomX][randomY + sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX + sizeOfInclusions][randomY - sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX - sizeOfInclusions][randomY + sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX - sizeOfInclusions][randomY].color != Color.BLACK
                                && matrix[randomX][randomY - sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX - sizeOfInclusions][randomY - sizeOfInclusions].color != Color.BLACK) {

                            int x = sizeOfInclusions;
                            int y = 0;
                            int xChange = 1 - (sizeOfInclusions << 1);
                            int yChange = 0;
                            int radiusError = 0;

                            while (x >= y) {
                                for (int i = randomX - x; i <= randomX + x; i++) {
                                    matrix[i][randomY + y].color = Color.BLACK;
                                    matrix[i][randomY + y].id = Cell.idColor(matrix[i][randomY + y].color);
                                    matrix[i][randomY - y].color = Color.BLACK;
                                    matrix[i][randomY - y].id = Cell.idColor(matrix[i][randomY - y].color);
                                }
                                for (int i = randomX - y; i <= randomX + y; i++) {
                                    matrix[i][randomY + x].color = Color.BLACK;
                                    matrix[i][randomY + x].id = Cell.idColor(matrix[i][randomY + x].color);
                                    matrix[i][randomY - x].color = Color.BLACK;
                                    matrix[i][randomY - x].id = Cell.idColor(matrix[i][randomY - x].color);
                                }
                                y++;
                                radiusError += yChange;
                                yChange += 2;
                                if (((radiusError << 1) + xChange) > 0) {
                                    x--;
                                    radiusError += xChange;
                                    xChange += 2;
                                }
                            }
                            inclusions--;
                        }
                    }
                }
                refresh();
            }
            if (typeOfInclusionComboBox.getSelectedItem().toString().equals("square")) {
                while (inclusions > 0) {
                    randomX = Math.abs(random.nextInt()) % width;
                    randomY = Math.abs(random.nextInt()) % height;

                    if (randomX > sizeOfInclusions && randomX < width - sizeOfInclusions
                            && randomY > sizeOfInclusions && randomY < height - sizeOfInclusions) {
                        if ((matrix[randomX][randomY].id != matrix[randomX + 1][randomY + 1].id
                                || matrix[randomX][randomY].id != matrix[randomX + 1][randomY].id
                                || matrix[randomX][randomY].id != matrix[randomX][randomY + 1].id
                                || matrix[randomX][randomY].id != matrix[randomX + 1][randomY - 1].id
                                || matrix[randomX][randomY].id != matrix[randomX - 1][randomY + 1].id
                                || matrix[randomX][randomY].id != matrix[randomX - 1][randomY].id
                                || matrix[randomX][randomY].id != matrix[randomX][randomY - 1].id
                                || matrix[randomX][randomY].id != matrix[randomX - 1][randomY - 1].id)
                                && matrix[randomX + sizeOfInclusions][randomY + sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX + sizeOfInclusions][randomY].color != Color.BLACK
                                && matrix[randomX][randomY + sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX + sizeOfInclusions][randomY - sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX - sizeOfInclusions][randomY + sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX - sizeOfInclusions][randomY].color != Color.BLACK
                                && matrix[randomX][randomY - sizeOfInclusions].color != Color.BLACK
                                && matrix[randomX - sizeOfInclusions][randomY - sizeOfInclusions].color != Color.BLACK) {

                            int a = (int) (sizeOfInclusions / Math.sqrt(2));
                            for (int i = randomX - (a / 2); i < randomX + (a / 2); i++) {
                                for (int j = randomY - (a / 2); j < randomY + (a / 2); j++) {
                                    matrix[i][j].color = Color.BLACK;
                                    matrix[i][j].id = Cell.idColor(matrix[i][j].color);
                                }
                            }
                            inclusions--;
                        }
                    }
                }
                refresh();
            }
        }
    }//GEN-LAST:event_addInclusionsButtonActionPerformed

    private void typeOfInclusionComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_typeOfInclusionComboBoxItemStateChanged
        // TODO add your handling code here:
        if (typeOfInclusionComboBox.getSelectedItem().toString().equals("circular")) {
            jLabel3.setText("Radius r");
        } else if (typeOfInclusionComboBox.getSelectedItem().toString().equals("square")) {
            jLabel3.setText("Diagonal d");
        }
    }//GEN-LAST:event_typeOfInclusionComboBoxItemStateChanged

    private void GBAllGrainsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GBAllGrainsButtonActionPerformed
        // TODO add your handling code here:
        clearSpaceButton.setEnabled(true);
        GBsizeLabel.setText("GB size = " + ++GBsize);
        if (startButton.getText().equals("Finished")) {
            Cell black = new Cell();
            black.color = Color.BLACK;
            black.id = Cell.idColor(black.color);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (i > 0 && i < width - 1 && j > 0 && j < height - 1) {
                        //matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != matrix[i][j].id) {
                            matrix2[i][j - 1] = black;
                        }
                        if (matrix[i + 1][j - 1].id != matrix[i][j].id) {
                            matrix2[i + 1][j - 1] = black;
                        }
                        if (matrix[i + 1][j].id != matrix[i][j].id) {
                            matrix2[i + 1][j] = black;
                        }
                        if (matrix[i + 1][j + 1].id != matrix[i][j].id) {
                            matrix2[i + 1][j + 1] = black;
                        }
                        if (matrix[i][j + 1].id != matrix[i][j].id) {
                            matrix2[i][j + 1] = black;
                        }
                        if (matrix[i - 1][j + 1].id != matrix[i][j].id) {
                            matrix2[i - 1][j + 1] = black;
                        }
                        if (matrix[i - 1][j].id != matrix[i][j].id) {
                            matrix2[i - 1][j] = black;
                        }
                        if (matrix[i - 1][j - 1].id != matrix[i][j].id) {
                            matrix2[i - 1][j - 1] = black;
                        }
                    } else if (i == 0 && i < width - 1 && j > 0 && j < height - 1) {
                        //matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != matrix[i][j].id) {
                            matrix2[i][j - 1] = black;
                        }
                        if (matrix[i + 1][j - 1].id != matrix[i][j].id) {
                            matrix2[i + 1][j - 1] = black;
                        }
                        if (matrix[i + 1][j].id != matrix[i][j].id) {
                            matrix2[i + 1][j] = black;
                        }
                        if (matrix[i + 1][j + 1].id != matrix[i][j].id) {
                            matrix2[i + 1][j + 1] = black;
                        }
                        if (matrix[i][j + 1].id != matrix[i][j].id) {
                            matrix2[i][j + 1] = black;
                        }
                    } else if (i > 0 && i < width - 1 && j == 0 && j < height - 1) {
                        //matrix2[i][j] = matrix[i][j];
                        if (matrix[i + 1][j].id != matrix[i][j].id) {
                            matrix2[i + 1][j] = black;
                        }
                        if (matrix[i + 1][j + 1].id != matrix[i][j].id) {
                            matrix2[i + 1][j + 1] = black;
                        }
                        if (matrix[i][j + 1].id != matrix[i][j].id) {
                            matrix2[i][j + 1] = black;
                        }
                        if (matrix[i - 1][j + 1].id != matrix[i][j].id) {
                            matrix2[i - 1][j + 1] = black;
                        }
                        if (matrix[i - 1][j].id != matrix[i][j].id) {
                            matrix2[i - 1][j] = black;
                        }
                    } else if (i > 0 && i == width - 1 && j > 0 && j < height - 1) {
                        //matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != matrix[i][j].id) {
                            matrix2[i][j - 1] = black;
                        }
                        if (matrix[i - 1][j - 1].id != matrix[i][j].id) {
                            matrix2[i - 1][j - 1] = black;
                        }
                        if (matrix[i][j + 1].id != matrix[i][j].id) {
                            matrix2[i][j + 1] = black;
                        }
                        if (matrix[i - 1][j + 1].id != matrix[i][j].id) {
                            matrix2[i - 1][j + 1] = black;
                        }
                        if (matrix[i - 1][j].id != matrix[i][j].id) {
                            matrix2[i - 1][j] = black;
                        }
                    } else if (i > 0 && i < width - 1 && j > 0 && j == height - 1) {
                        //matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != matrix[i][j].id) {
                            matrix2[i][j - 1] = black;
                        }
                        if (matrix[i + 1][j - 1].id != matrix[i][j].id) {
                            matrix2[i + 1][j - 1] = black;
                        }
                        if (matrix[i + 1][j].id != matrix[i][j].id) {
                            matrix2[i + 1][j] = black;
                        }
                        if (matrix[i - 1][j].id != matrix[i][j].id) {
                            matrix2[i - 1][j] = black;
                        }
                        if (matrix[i - 1][j - 1].id != matrix[i][j].id) {
                            matrix2[i - 1][j - 1] = black;
                        }
                    } else if (i == 0 && j == 0) {
                        //matrix2[i][j] = matrix[i][j];
                        if (matrix[i + 1][j].id != matrix[i][j].id) {
                            matrix2[i + 1][j] = black;
                        }
                        if (matrix[i + 1][j + 1].id != matrix[i][j].id) {
                            matrix2[i + 1][j + 1] = black;
                        }
                        if (matrix[i][j + 1].id != matrix[i][j].id) {
                            matrix2[i][j + 1] = black;
                        }
                    } else if (i == width - 1 && j == 0) {
                        //matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j + 1].id != matrix[i][j].id) {
                            matrix2[i][j + 1] = black;
                        }
                        if (matrix[i - 1][j + 1].id != matrix[i][j].id) {
                            matrix2[i - 1][j + 1] = black;
                        }
                        if (matrix[i - 1][j].id != matrix[i][j].id) {
                            matrix2[i - 1][j] = black;
                        }
                    } else if (i == 0 && j == height - 1) {
                        //matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != matrix[i][j].id) {
                            matrix2[i][j - 1] = black;
                        }
                        if (matrix[i + 1][j - 1].id != matrix[i][j].id) {
                            matrix2[i + 1][j - 1] = black;
                        }
                        if (matrix[i + 1][j].id != matrix[i][j].id) {
                            matrix2[i + 1][j] = black;
                        }
                    } else if (i == width - 1 && j == height - 1) {
                        //matrix2[i][j] = matrix[i][j];
                        if (matrix[i][j - 1].id != matrix[i][j].id) {
                            matrix2[i][j - 1] = black;
                        }
                        if (matrix[i - 1][j - 1].id != matrix[i][j].id) {
                            matrix2[i - 1][j - 1] = black;
                        }
                        if (matrix[i - 1][j].id != matrix[i][j].id) {
                            matrix2[i - 1][j] = black;
                        }
                    }

                    /*if (matrix[i][j].id == -1) {
                        if (i == 0 && j == 0) {
                            matrix2[i][j] = matrix[i + 1][j + 1];
                        }
                        if (i == width - 1 && j == 0) {
                            matrix2[i][j] = matrix[i - 1][j + 1];
                        }
                        if (i == 0 && j == height - 1) {
                            matrix2[i][j] = matrix[i + 1][j - 1];
                        }
                        if (i == width - 1 && j == height - 1) {
                            matrix2[i][j] = matrix[i - 1][j - 1];
                        }
                    }*/
                }
            }
        }
        change(matrix, matrix2, width, height);
        refresh();
        checkPercentageOfGB();
    }//GEN-LAST:event_GBAllGrainsButtonActionPerformed

    private void clearSpaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearSpaceButtonActionPerformed
        // TODO add your handling code here:
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (matrix[i][j].color != Color.BLACK) {
                    matrix2[i][j] = new Cell();
                }
            }
        }
        change(matrix, matrix2, width, height);
        refresh();
        startButton.setText("Start");
        startButton.setEnabled(true);
        addGrainsButton.setEnabled(true);
        addInclusionsButton.setEnabled(true);
        GBAllGrainsButton.setEnabled(false);
        GBSingleGrainsButton.setEnabled(false);
        clearSpaceButton.setEnabled(false);
        jProgressBar1.setValue(0);
        jProgressBar1.setString(0 + "% Complete");
    }//GEN-LAST:event_clearSpaceButtonActionPerformed

    private void GBSingleGrainsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GBSingleGrainsButtonActionPerformed
        // TODO add your handling code here:
        clearSpaceButton.setEnabled(true);
        Cell black = new Cell();
        black.color = Color.BLACK;
        black.id = Cell.idColor(black.color);
        Cell white = new Cell();
        Cell selected = new Cell();
        int temp = 1;
        Random random = new Random();
        randomX = 0;
        randomY = 0;
        while (temp > 0) {
            randomX = Math.abs(random.nextInt()) % width;
            randomY = Math.abs(random.nextInt()) % height;

            if (matrix[randomX][randomY] != black && matrix[randomX][randomY] != white) {
                selected = matrix[randomX][randomY];
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (matrix[i][j].id == selected.id) {
                            if (i > 0 && i < width - 1 && j > 0 && j < height - 1) {
                                //matrix2[i][j] = matrix[i][j];
                                if (matrix[i][j - 1].id != matrix[i][j].id) {
                                    matrix2[i][j - 1] = black;
                                }
                                if (matrix[i + 1][j - 1].id != matrix[i][j].id) {
                                    matrix2[i + 1][j - 1] = black;
                                }
                                if (matrix[i + 1][j].id != matrix[i][j].id) {
                                    matrix2[i + 1][j] = black;
                                }
                                if (matrix[i + 1][j + 1].id != matrix[i][j].id) {
                                    matrix2[i + 1][j + 1] = black;
                                }
                                if (matrix[i][j + 1].id != matrix[i][j].id) {
                                    matrix2[i][j + 1] = black;
                                }
                                if (matrix[i - 1][j + 1].id != matrix[i][j].id) {
                                    matrix2[i - 1][j + 1] = black;
                                }
                                if (matrix[i - 1][j].id != matrix[i][j].id) {
                                    matrix2[i - 1][j] = black;
                                }
                                if (matrix[i - 1][j - 1].id != matrix[i][j].id) {
                                    matrix2[i - 1][j - 1] = black;
                                }
                            } else if (i == 0 && i < width - 1 && j > 0 && j < height - 1) {
                                //matrix2[i][j] = matrix[i][j];
                                if (matrix[i][j - 1].id != matrix[i][j].id) {
                                    matrix2[i][j - 1] = black;
                                }
                                if (matrix[i + 1][j - 1].id != matrix[i][j].id) {
                                    matrix2[i + 1][j - 1] = black;
                                }
                                if (matrix[i + 1][j].id != matrix[i][j].id) {
                                    matrix2[i + 1][j] = black;
                                }
                                if (matrix[i + 1][j + 1].id != matrix[i][j].id) {
                                    matrix2[i + 1][j + 1] = black;
                                }
                                if (matrix[i][j + 1].id != matrix[i][j].id) {
                                    matrix2[i][j + 1] = black;
                                }
                            } else if (i > 0 && i < width - 1 && j == 0 && j < height - 1) {
                                //matrix2[i][j] = matrix[i][j];
                                if (matrix[i + 1][j].id != matrix[i][j].id) {
                                    matrix2[i + 1][j] = black;
                                }
                                if (matrix[i + 1][j + 1].id != matrix[i][j].id) {
                                    matrix2[i + 1][j + 1] = black;
                                }
                                if (matrix[i][j + 1].id != matrix[i][j].id) {
                                    matrix2[i][j + 1] = black;
                                }
                                if (matrix[i - 1][j + 1].id != matrix[i][j].id) {
                                    matrix2[i - 1][j + 1] = black;
                                }
                                if (matrix[i - 1][j].id != matrix[i][j].id) {
                                    matrix2[i - 1][j] = black;
                                }
                            } else if (i > 0 && i == width - 1 && j > 0 && j < height - 1) {
                                //matrix2[i][j] = matrix[i][j];
                                if (matrix[i][j - 1].id != matrix[i][j].id) {
                                    matrix2[i][j - 1] = black;
                                }
                                if (matrix[i - 1][j - 1].id != matrix[i][j].id) {
                                    matrix2[i - 1][j - 1] = black;
                                }
                                if (matrix[i][j + 1].id != matrix[i][j].id) {
                                    matrix2[i][j + 1] = black;
                                }
                                if (matrix[i - 1][j + 1].id != matrix[i][j].id) {
                                    matrix2[i - 1][j + 1] = black;
                                }
                                if (matrix[i - 1][j].id != matrix[i][j].id) {
                                    matrix2[i - 1][j] = black;
                                }
                            } else if (i > 0 && i < width - 1 && j > 0 && j == height - 1) {
                                //matrix2[i][j] = matrix[i][j];
                                if (matrix[i][j - 1].id != matrix[i][j].id) {
                                    matrix2[i][j - 1] = black;
                                }
                                if (matrix[i + 1][j - 1].id != matrix[i][j].id) {
                                    matrix2[i + 1][j - 1] = black;
                                }
                                if (matrix[i + 1][j].id != matrix[i][j].id) {
                                    matrix2[i + 1][j] = black;
                                }
                                if (matrix[i - 1][j].id != matrix[i][j].id) {
                                    matrix2[i - 1][j] = black;
                                }
                                if (matrix[i - 1][j - 1].id != matrix[i][j].id) {
                                    matrix2[i - 1][j - 1] = black;
                                }
                            } else if (i == 0 && j == 0) {
                                //matrix2[i][j] = matrix[i][j];
                                if (matrix[i + 1][j].id != matrix[i][j].id) {
                                    matrix2[i + 1][j] = black;
                                }
                                if (matrix[i + 1][j + 1].id != matrix[i][j].id) {
                                    matrix2[i + 1][j + 1] = black;
                                }
                                if (matrix[i][j + 1].id != matrix[i][j].id) {
                                    matrix2[i][j + 1] = black;
                                }
                            } else if (i == width - 1 && j == 0) {
                                //matrix2[i][j] = matrix[i][j];
                                if (matrix[i][j + 1].id != matrix[i][j].id) {
                                    matrix2[i][j + 1] = black;
                                }
                                if (matrix[i - 1][j + 1].id != matrix[i][j].id) {
                                    matrix2[i - 1][j + 1] = black;
                                }
                                if (matrix[i - 1][j].id != matrix[i][j].id) {
                                    matrix2[i - 1][j] = black;
                                }
                            } else if (i == 0 && j == height - 1) {
                                //matrix2[i][j] = matrix[i][j];
                                if (matrix[i][j - 1].id != matrix[i][j].id) {
                                    matrix2[i][j - 1] = black;
                                }
                                if (matrix[i + 1][j - 1].id != matrix[i][j].id) {
                                    matrix2[i + 1][j - 1] = black;
                                }
                                if (matrix[i + 1][j].id != matrix[i][j].id) {
                                    matrix2[i + 1][j] = black;
                                }
                            } else if (i == width - 1 && j == height - 1) {
                                //matrix2[i][j] = matrix[i][j];
                                if (matrix[i][j - 1].id != matrix[i][j].id) {
                                    matrix2[i][j - 1] = black;
                                }
                                if (matrix[i - 1][j - 1].id != matrix[i][j].id) {
                                    matrix2[i - 1][j - 1] = black;
                                }
                                if (matrix[i - 1][j].id != matrix[i][j].id) {
                                    matrix2[i - 1][j] = black;
                                }
                            }
                        }
                    }
                }
                change(matrix, matrix2, width, height);
                refresh();
                checkPercentageOfGB();
            }
            temp = 0;
        }

    }//GEN-LAST:event_GBSingleGrainsButtonActionPerformed

    private void generateMonteCarloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateMonteCarloActionPerformed
        // TODO add your handling code here:
        stateNumberList.clear();
        stateNumber = Integer.parseInt(stateNumberTextField.getText());
        counter = 0;
        //Cell.randomColor();
        Random random = new Random();
        randomColor = 0;
        while (stateNumber > 0) {
            stateNumberList.add(Cell.randomColor());
            stateNumber--;
        }
        stateNumber = Integer.parseInt(stateNumberTextField.getText());
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (matrix[i][j].id == -1) {
                    randomColor = Math.abs(random.nextInt()) % stateNumber;
                    matrix[i][j].color = stateNumberList.get(randomColor);
                    matrix[i][j].id = Cell.idColor(matrix[randomX][randomY].color);
                }
            }
        }

        stateNumberList2 = stateNumberList;
        colorComboBox.setModel(new DefaultComboBoxModel(stateNumberList2.toArray()));
        refresh();


    }//GEN-LAST:event_generateMonteCarloActionPerformed

    private void startMonteCarloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startMonteCarloActionPerformed
        // TODO add your handling code here:
        playMC = true;
        iterationMC = Integer.parseInt(MCSTextField.getText());
        if (playMC) {
            startButton.setText("Pause");
            addInclusionsButton.setEnabled(false);

        } else {
            startButton.setText("Resume");
        }

        
        /*for (int i = 0; i < iterationMC; i++) {
            monteCarlo();
            refresh();
            System.out.println("iteracja: " + i);
        }*/

    }//GEN-LAST:event_startMonteCarloActionPerformed

    private void colorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorComboBoxActionPerformed
        // TODO add your handling code here:
        if (colorComboBox.getSelectedItem() != null) {
            colorComboBox.setBackground(stateNumberList2.get(colorComboBox.getSelectedIndex()));
        }
    }//GEN-LAST:event_colorComboBoxActionPerformed

    private void selectGrainsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectGrainsButtonActionPerformed
        // TODO add your handling code here:
        dualPhaseColor = stateNumberList2.get(colorComboBox.getSelectedIndex());
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (matrix[i][j].color != dualPhaseColor) {
                    matrix2[i][j] = new Cell();
                }
            }
        }
        change(matrix, matrix2, width, height);
        refresh();
        startButton.setText("Start");
        startButton.setEnabled(true);
        addGrainsButton.setEnabled(true);
        addInclusionsButton.setEnabled(true);
        GBAllGrainsButton.setEnabled(false);
        GBSingleGrainsButton.setEnabled(false);
        clearSpaceButton.setEnabled(false);
        jProgressBar1.setValue(0);
        jProgressBar1.setString(0 + "% Complete");
    }//GEN-LAST:event_selectGrainsButtonActionPerformed

    private void switchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchButtonActionPerformed
        // TODO add your handling code here:
        if (switched == false) {
            switched = true;
            energyDistribution(matrix);
            refresh2();
        } else if (switched == true) {
            switched = false;
            refresh();
        }
        //podmieniać matrix zamiast paneli!
    }//GEN-LAST:event_switchButtonActionPerformed

    private void homogenousToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_homogenousToggleButtonActionPerformed
        // TODO add your handling code here:
        if (homogenousToggleButton.isSelected() == true) {
            jLabel11.setVisible(false);
            energyOnEdgesTextField.setVisible(false);
        } else {
            jLabel11.setVisible(true);
            energyOnEdgesTextField.setVisible(true);
        }
    }//GEN-LAST:event_homogenousToggleButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MultiscaleModellingFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MultiscaleModellingFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MultiscaleModellingFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MultiscaleModellingFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MultiscaleModellingFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton GBAllGrainsButton;
    private javax.swing.JButton GBSingleGrainsButton;
    private javax.swing.JLabel GBsizeLabel;
    private javax.swing.JTextField MCSTextField;
    private javax.swing.JButton addGrainsButton;
    private javax.swing.JButton addInclusionsButton;
    private javax.swing.JTextField amountOfInclusionsTextField;
    private javax.swing.JButton clearSpaceButton;
    private javax.swing.JComboBox<String> colorComboBox;
    private javax.swing.JButton createButton;
    private javax.swing.JTextField energyInsideTextField;
    private javax.swing.JTextField energyOnEdgesTextField;
    private javax.swing.JMenuItem exportMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JButton generateMonteCarlo;
    private javax.swing.JTextField heightTextField;
    private javax.swing.JToggleButton homogenousToggleButton;
    private javax.swing.JMenuItem importMenuItem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JMenu microstructureMenu;
    private javax.swing.JTextField numbersOfGrainsTextField;
    private javax.swing.JLabel perOfGBLabel;
    private javax.swing.JButton selectGrainsButton;
    private javax.swing.JTextField sizeOfInclusionsTextField;
    private javax.swing.JButton startButton;
    private javax.swing.JButton startMonteCarlo;
    private javax.swing.JTextField stateNumberTextField;
    private javax.swing.JButton switchButton;
    private javax.swing.JComboBox<String> typeOfInclusionComboBox;
    private javax.swing.JComboBox<String> typeOfNeighborhoodComboBox;
    private javax.swing.JTextField widthTextField;
    // End of variables declaration//GEN-END:variables
}
