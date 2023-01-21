import FFM.FileMaster;
import acm.graphics.GImage;
import freshui.FreshUI;
import freshui.graphics.FButton;
import freshui.gui.Header;
import freshui.gui.input.Input;
import freshui.io.Printer;
import freshui.util.FColor;
import freshui.util.Resizer;
import svu.csc213.Dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;


public class File_Scanner extends FreshUI {

    final int LEFT_PADDING = 10;
    final int TOP_PADDING = 10;
    final int INPUT_SPACING = 50;
    final int INPUT_HEIGHT = 40;

    Input searchPath,savePath;
    FButton selectDirectory, selectOutput;
    static JLabel status, fileStatus, folderStatus;

    /* FOR TESTING PURPOSES
    static final String DEFAULT_DIRECTORY = FileMaster.DOCUMENTS_FOLDER;
    static final String DEFAULT_OUTPUT_LOC = FileMaster.DESKTOP_FOLDER;
     */

    static final String DEFAULT_DIRECTORY = "";
    static final String DEFAULT_OUTPUT_LOC = "";

    // colors
    final public Color DEFAULT_COLOR = new Color(57, 117, 175);
    final public Color HOVER_COLOR = FColor.darker(DEFAULT_COLOR,0.87);
    final public Color SEARCH_INPUT_COLOR = new Color(210, 148, 82);
    final public Color SAVE_INPUT_COLOR = new Color(125, 170, 213);
    final public Color HEADER_COLOR = new Color(165, 218, 255);

    static PrintWriter out;

    static GImage icon = new GImage("folder-icon-noahl.png");

    static int filesFound;
    static int directoriesFound;

    public void init(){
        filesFound = 0;
        directoriesFound = 0;

        Resizer resizer = new Resizer(this);
        setProgramName("File Scanner");
        setSize(450,320);

        Header header = new Header(getWidth(),"File Finder",CENTER,this);
        add(header,0,0);
        header.setColor(HEADER_COLOR);
        header.setFont(new Font("Arial",Font.BOLD,20));

        // app icon
        icon.scale(0.06);
        add(icon,header.getWidth()/14,header.getHeight()/2-icon.getHeight()/2);

        /// region Input Initialization

        searchPath = new Input("Directory to search:",this);
        searchPath.setWidth(getWidth() - LEFT_PADDING*2);
        searchPath.setHeight(INPUT_HEIGHT);
        add(searchPath,LEFT_PADDING, header.getHeight() + TOP_PADDING);
        searchPath.setColor(SEARCH_INPUT_COLOR);
        searchPath.setCornerRadius(4);
        searchPath.setInputText(DEFAULT_DIRECTORY);

        savePath = new Input("Save results to:",this);
        savePath.setWidth(getWidth() - LEFT_PADDING*2);
        savePath.setHeight(INPUT_HEIGHT);
        add(savePath,LEFT_PADDING, searchPath.getY() + INPUT_SPACING);
        savePath.setColor(SAVE_INPUT_COLOR);
        savePath.setCornerRadius(4);
        savePath.setInputText(DEFAULT_OUTPUT_LOC);

        /// endregion

        /// region Start Buttons
        FButton startSearch = new FButton("Search Directories");
        startSearch.setSize(getWidth() - LEFT_PADDING*2, getHeight()/10);
        startSearch.setColor(new Color(54, 178, 224));
        startSearch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                startSearch();
            }

            @Override
            public void mouseEntered(MouseEvent e){
                startSearch.setColor(new Color(42, 147, 185));
            }

            @Override
            public void mouseExited(MouseEvent e){
                startSearch.setColor(new Color(54, 178, 224));
            }
        });
        add(startSearch,LEFT_PADDING, getHeight() - startSearch.getHeight() - TOP_PADDING);
        startSearch.setCornerRadius(4);
        /// endregion

        /// region Selection Buttons

        selectDirectory = new FButton("Select", 50,20);
        selectOutput = new FButton("Select",50,20);
        selectDirectory.setColor(FColor.darker(SEARCH_INPUT_COLOR,0.8));
        selectOutput.setColor(FColor.darker(SAVE_INPUT_COLOR,0.8));

        selectDirectory.setCornerRadius(3);
        add(selectDirectory, LEFT_PADDING+LEFT_PADDING,searchPath.getY()+INPUT_HEIGHT/4);

        selectOutput.setCornerRadius(3);
        add(selectOutput, selectDirectory.getX(), savePath.getY()+INPUT_HEIGHT/4);

        selectDirectory.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                searchPath.setInputText( chooseDir().getPath());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                selectDirectory.setColor(FColor.darker(SEARCH_INPUT_COLOR,0.9));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                selectDirectory.setColor(FColor.darker(SEARCH_INPUT_COLOR,0.8));
            }
        });
        selectOutput.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                savePath.setInputText(chooseDir().getPath());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                selectOutput.setColor(FColor.darker(SAVE_INPUT_COLOR,0.9));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                selectOutput.setColor(FColor.darker(SAVE_INPUT_COLOR,0.8));
            }
        });

        /// endregion

        status = new JLabel();
        status.setSize(getWidth()-LEFT_PADDING*2,status.getHeight());
        status.setForeground(Color.GRAY);
        status.setText("Waiting to Search...");
        add(status,0,0);
        status.setLocation(LEFT_PADDING, (int) (startSearch.getY()-TOP_PADDING-status.getHeight()));

        fileStatus = new JLabel("");
        fileStatus.setSize(getWidth()-LEFT_PADDING*2, fileStatus.getHeight());
        fileStatus.setForeground(new Color(29, 85, 131));
        fileStatus.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(fileStatus,0,0);
        fileStatus.setLocation(LEFT_PADDING, (int) (savePath.getY()+savePath.getHeight()+TOP_PADDING));

        folderStatus = new JLabel("");
        folderStatus.setSize(getWidth()-LEFT_PADDING*2,folderStatus.getHeight());
        folderStatus.setForeground(new Color(46, 100, 141));
        folderStatus.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(folderStatus,0,0);
        folderStatus.setLocation(LEFT_PADDING, fileStatus.getY()+fileStatus.getHeight()+40);

        updateFound();
    }

    private static void updateFound(){
        fileStatus.setText("Files Found: " + filesFound);
        folderStatus.setText("Directories Found: " + directoriesFound);
    }

    private void startSearch(){
        status.setText("Searching...");
        status.setForeground(new Color(48, 101, 166));
        Printer.println("SEARCH STARTED.");
        filesFound = 0;
        directoriesFound = 1;

        LocalDateTime start = LocalDateTime.now();

        /// region Search Functions

        String directoryPath = searchPath.getInputText();
        String outputPath = savePath.getInputText();

        // instantiate the file
        File output = new File(outputPath + "/Search Results -" + LocalDateTime.now().toString() +".txt/");
        try {
            out = new PrintWriter((File)output);
            out.println("\t DIRECTORY SEARCH : " + directoryPath);
            System.out.println("\t DIRECTORY SEARCH : " + directoryPath);
            out.println("\t Performed on: " + LocalTime.now());
            System.out.println("\t Performed on: " + LocalTime.now());
            out.println("============================");
            System.out.println("============================");
        } catch (FileNotFoundException var5) {
            throw new RuntimeException(var5);
        }

        File mainDir = new File(directoryPath);
        if (mainDir.exists() && mainDir.isDirectory()) {
            File[] arr = mainDir.listFiles();
            RecursivePrint(arr, 0);
        } else {
            Printer.println("\t! SEARCH FAILED - SPECIFIED LOCATION INVALID",Printer.RED);
        }

        /// endregion

        Printer.println("SEARCH FINISHED.",Printer.GREEN);
        Printer.print("Found a total of ", Printer.WHITE);
        Printer.print(directoriesFound + " directories", Printer.YELLOW);
        Printer.print(" and ", Printer.WHITE);
        Printer.print(filesFound + " files.", Printer.YELLOW);
        Printer.println("");

        Printer.println("Search Results Saved To: " + output.getPath(), Printer.BLUE);

        /// region Time Calculations
        LocalDateTime finish = LocalDateTime.now();
        int hours = finish.minusHours(start.getHour()).getHour();
        int minutes = finish.minusMinutes(start.getMinute()).getMinute();
        int seconds = finish.minusSeconds(start.getSecond()).getSecond();
        int nanos = finish.minusNanos(start.getNano()).getNano();

        /// endregion

        status.setText("Search Finished in " + hours + ":" + minutes + ":" + seconds +  "::" + nanos);
        status.setForeground(new Color(19, 155, 15));

        Dialog.showMessage("<html>Search Completed.<p>Found "+directoriesFound+" directories and "+filesFound+" files.</html>");

    }

    static void RecursivePrint(File[] arr, int level) {
        File[] var2 = arr;
        int var3 = arr.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            File f = var2[var4];

            for(int i = 0; i < level; ++i) {
                out.print("\t");
            }

            System.out.print("\t");
            if (f.isFile()) {
                out.println(f.getName());
                System.out.println(f.getName());
                filesFound++;
            } else if (f.isDirectory()) {
                out.println("[" + f.getName() + "]");
                System.out.println("[" + f.getName() + "]");
                directoriesFound++;

                try {
                    RecursivePrint(Objects.requireNonNull(f.listFiles()), level + 1);
                } catch (NullPointerException nio) {
                    Printer.println("\t ! ERROR ACCESSING FILES",Printer.RED);
                    Printer.println("\t\t" + f.getPath(),Printer.BLUE);
                    nio.printStackTrace();
                }
            }
        }

    }

    private File chooseDir(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return selectedFile;
        }else{
            return null;
        }
    }

    public static void main(String[] args) {
        new File_Scanner().start();
    }

}
