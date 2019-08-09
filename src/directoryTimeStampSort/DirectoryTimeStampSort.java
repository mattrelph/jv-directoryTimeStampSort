/*
directoryTimeStampSort - Java (SWT Windows app) version
Taking a directory full of files, and putting them in folders based on the date of their last modification

Works good for lots of things, logs, pictures, piles of HL7 pharmacy requests, you name it!

Some error checking. Should stop and report to shell on any error As always, use at your own risk. 
 */

 /*
  * This is the file for the main Window - Currently, there are no other windows for this utility. 
  * */



package directoryTimeStampSort;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;

public class DirectoryTimeStampSort {

	protected Shell shlDurectiryTimeStamp;
	private Text debugTextBox;

    private static Boolean moveFlag;      //Do we move the files, or do we just copy
    private static Boolean promptFlag;    //Do we want to be prompted on most decisions, or just get on with the show?
    private static Boolean overwriteFlag; //If a file already exists, do we want to overwrite or make a duplicate?
    private static char sortByChar;    //Determines if we sort by file year, month, or date
    private static Boolean deBugTimeFlag; //Only here for debugging purposes. Make true if you need it. 
    	
	private Label lblDestinationDirectory;
	private Text SourceDirTextbox;
	private Text DestinationDirTextBox;
	private Button btnBrowseSourceDir;
	private Button btnBrowseDestinationDir;
	private Label lblActionType;
	
	

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {

		
        moveFlag = false;
        promptFlag = true;
        overwriteFlag = true;
        sortByChar = 'y';

        
        deBugTimeFlag = false;  //Only here for debugging purposes. Make true if you need it.
		try {
			DirectoryTimeStampSort window = new DirectoryTimeStampSort();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlDurectiryTimeStamp.open();
		shlDurectiryTimeStamp.layout();
		while (!shlDurectiryTimeStamp.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlDurectiryTimeStamp = new Shell();
		shlDurectiryTimeStamp.setSize(727, 489);
		shlDurectiryTimeStamp.setText("Directory Time Stamp Sort");
		
		Label lblSourceDirectory = new Label(shlDurectiryTimeStamp, SWT.NONE);
		lblSourceDirectory.setBounds(10, 34, 123, 15);
		lblSourceDirectory.setText("Source Directory");
		
		debugTextBox = new Text(shlDurectiryTimeStamp, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		debugTextBox.setEditable(false);
		debugTextBox.setBounds(10, 230, 671, 210);
		
		Button btnRunIt = new Button(shlDurectiryTimeStamp, SWT.NONE);
		btnRunIt.setEnabled(false);
		btnRunIt.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		btnRunIt.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {

				debugTextBox.setText("Checking arguments...\n");
				Boolean continueFlag = true;
				if (promptFlag)
				{
					if (checkArgs(SourceDirTextbox.getText(), DestinationDirTextBox.getText()))
					{
		            	MessageBox sortMessageBox = new MessageBox(shlDurectiryTimeStamp, SWT.ICON_WARNING | SWT.YES | SWT.NO);
		            	sortMessageBox.setText("Sort Files");
		            	sortMessageBox.setMessage("Proceed with sort?\n(Files May Be Moved)");
		                                   
		                if (sortMessageBox.open() == SWT.YES)
		                {
		                    debugTextBox.append("Proceed with sort: Yes\n");
		                    continueFlag = true;
		                }
		                else
		                {
		                    debugTextBox.append("Proceed with sort: No\n");
		                    continueFlag = false;
		                }
						
					}
					else
					{
						debugTextBox.append("Error with source/destination directories. Try again.\n");
					}
					
				}
				if (continueFlag)
				{
					sortFiles(SourceDirTextbox.getText(), DestinationDirTextBox.getText());
				}
				
			}
		});
		btnRunIt.setBounds(10, 184, 671, 25);
		btnRunIt.setText("SORT!");
		
		lblDestinationDirectory = new Label(shlDurectiryTimeStamp, SWT.NONE);
		lblDestinationDirectory.setBounds(10, 70, 123, 15);
		lblDestinationDirectory.setText("Destination Directory");
		
		SourceDirTextbox = new Text(shlDurectiryTimeStamp, SWT.BORDER);
		SourceDirTextbox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
	            if ((SourceDirTextbox.getText().length() != 0) && (DestinationDirTextBox.getText().length() != 0))
	            {
	            	debugTextBox.setText("Source and Destination entered: Ready to Sort\n");
	            	btnRunIt.setEnabled(true);
	            }
	            else
	            {
	                debugTextBox.setText("Need Source and Destination to Sort\n");
	                btnRunIt.setEnabled(false);
	            }		
			}
		});
		SourceDirTextbox.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
		
			}
		});
		SourceDirTextbox.setBounds(143, 34, 440, 21);
		
		DestinationDirTextBox = new Text(shlDurectiryTimeStamp, SWT.BORDER);
		DestinationDirTextBox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
	            if ((SourceDirTextbox.getText().length() != 0) && (DestinationDirTextBox.getText().length() != 0))
	            {
	            	debugTextBox.setText("Source and Destination entered: Ready to Sort\n");
	            	btnRunIt.setEnabled(true);
	            }
	            else
	            {
	                debugTextBox.setText("Need Source and Destination to Sort\n");
	                btnRunIt.setEnabled(false);
	            }					


			}
		});
		DestinationDirTextBox.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
			}	

		});
		DestinationDirTextBox.setBounds(143, 70, 440, 21);
		
		btnBrowseSourceDir = new Button(shlDurectiryTimeStamp, SWT.NONE);
		btnBrowseSourceDir.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(shlDurectiryTimeStamp);
				dlg.setFilterPath("%userprofile%\\documents");
				dlg.setText("Choose source directory");
				dlg.setMessage("Select a directory");
				String dir = dlg.open();
		        if (dir != null) {
		        	SourceDirTextbox.setText(dir);
		          }					
			}
		});
		btnBrowseSourceDir.setBounds(606, 34, 75, 25);
		btnBrowseSourceDir.setText("Browse...");
		
		btnBrowseDestinationDir = new Button(shlDurectiryTimeStamp, SWT.NONE);
		btnBrowseDestinationDir.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(shlDurectiryTimeStamp);
				dlg.setFilterPath("%userprofile%\\documents");
				dlg.setText("Choose destination directory");
				dlg.setMessage("Select a directory");
				String dir = dlg.open();
		        if (dir != null) {
		        	DestinationDirTextBox.setText(dir);
		          }					
			}
		});
		btnBrowseDestinationDir.setBounds(606, 70, 75, 25);
		btnBrowseDestinationDir.setText("Browse....");
		
		lblActionType = new Label(shlDurectiryTimeStamp, SWT.NONE);
		lblActionType.setBounds(10, 111, 91, 15);
		lblActionType.setText("Action Type");
		
		Combo MoveCombo = new Combo(shlDurectiryTimeStamp, SWT.NONE);
		MoveCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
	            if (MoveCombo.getSelectionIndex() == 0)
	            {
	                moveFlag = false;
	            }
	            else
	            {
	                moveFlag = true;
	            }
	            debugTextBox.setText("Move Flag:" + moveFlag + "\n");				
			}
			
		});
		MoveCombo.setItems(new String[] {"Copy Files Only", "Move All Files"});
		MoveCombo.setBounds(10, 132, 135, 23);
		MoveCombo.select(0);
		
		Label lblPrompts = new Label(shlDurectiryTimeStamp, SWT.NONE);
		lblPrompts.setBounds(163, 111, 91, 15);
		lblPrompts.setText("Prompts");
		
		Combo PromptsCombo = new Combo(shlDurectiryTimeStamp, SWT.NONE);
		PromptsCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (PromptsCombo.getSelectionIndex() == 0)
	            {
					promptFlag = true;
	            }
	            else
	            {
	            	promptFlag = false;
	            }
	            debugTextBox.setText("Prompt Flag:" + promptFlag + "\n");	
			}
		});
		PromptsCombo.setItems(new String[] {"Prompt on Conflict", "No Prompts"});
		PromptsCombo.setBounds(163, 132, 135, 23);
		PromptsCombo.select(0);
		
		Label lblSortBy = new Label(shlDurectiryTimeStamp, SWT.NONE);
		lblSortBy.setBounds(314, 111, 91, 15);
		lblSortBy.setText("Sort By");
		
		Combo SortByCombo = new Combo(shlDurectiryTimeStamp, SWT.NONE);
		SortByCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
	            if (SortByCombo.getSelectionIndex() == 0)
	            {
	                sortByChar = 'y';
	            }
	            else if (SortByCombo.getSelectionIndex() == 1)
	            {
	                sortByChar = 'm';
	            }
	            else
	            {
	                sortByChar = 'd';
	            }
	            debugTextBox.setText("SortBy:" + sortByChar + "\n");
			}
			
		});
		SortByCombo.setItems(new String[] {"Year", "Month", "Day"});
		SortByCombo.setBounds(314, 132, 135, 23);
		SortByCombo.select(0);
		
		Label lblOverwrite = new Label(shlDurectiryTimeStamp, SWT.NONE);
		lblOverwrite.setBounds(463, 111, 91, 15);
		lblOverwrite.setText("Default Action");
		
		Combo OverwriteCombo = new Combo(shlDurectiryTimeStamp, SWT.NONE);
		OverwriteCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (OverwriteCombo.getSelectionIndex() == 0)
	            {
					overwriteFlag = true;
	            }
	            else
	            {
	            	overwriteFlag = false;
	            }
	            debugTextBox.setText("Overwrite Flag:" + overwriteFlag + "\n");	
			}
		});
		OverwriteCombo.setItems(new String[] {"Overwrite", "Make Duplicate"});
		OverwriteCombo.setBounds(463, 132, 135, 23);
		OverwriteCombo.select(0);
		
		Label lblDescription = new Label(shlDurectiryTimeStamp, SWT.NONE);
		lblDescription.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblDescription.setBounds(10, 10, 691, 15);
		lblDescription.setText("This Program takes the files in the source directory and sorts them into folders in the output directory by their time stamp. ");

		
        debugTextBox.setText("Idle\n");
        if (deBugTimeFlag)      
        {
            //Pre-Populate these for to speed up testing process
        	SourceDirTextbox.setText("C:\\input");
            DestinationDirTextBox.setText("C:\\output");
            btnRunIt.setEnabled(true);
        }		


	}
	

    private Boolean checkArgs(String sourcePathString, String destinationPathString)
    {
    	Boolean continueFlag = false;
        
        //Test source directory
    	File sourceDirectory = new File(sourcePathString);
    	if (sourceDirectory.exists() && sourceDirectory.isDirectory())
        {
            //Source Directory Exists, we can continue
            continueFlag = true;            
            debugTextBox.append("Source Path:" +  sourcePathString + "\nSource = Good Path \n");
            //Test destination directory
            File destinationDirectory = new File(destinationPathString);
            if (destinationDirectory.exists() && destinationDirectory.isDirectory() && continueFlag)
            {
                //Destination directory exists, we can continue
            	debugTextBox.append("Destination Path:" + destinationPathString + "\nDestination = Good Path \n");
                continueFlag = true;
            }
            else
            {
                //Destination directory doesn't exist, maybe we can create it?
            	debugTextBox.append("Destination Path:" + destinationPathString + "\nDestination = Bad Path \n");
                continueFlag = false;

                Boolean createNewDestinationFlag = true;
                if (promptFlag)
                {
                	MessageBox mkdirMessageBox = new MessageBox(shlDurectiryTimeStamp, SWT.ICON_WARNING | SWT.YES | SWT.NO);
                	mkdirMessageBox.setText("Destination Directory Error");
                	mkdirMessageBox.setMessage("Destination Directory doesn't exist. \nAttempt to create\n\'" + destinationPathString + "\'?");
                                       
                    if (mkdirMessageBox.open() == SWT.YES)
                    {
                        createNewDestinationFlag = true;
                        debugTextBox.append("Create New Destination Directory: Yes\n");
                    }
                    else
                    {
                        createNewDestinationFlag = false;
                        debugTextBox.append("Create New Destination Directory: No\n");
                    }
                }
                if (createNewDestinationFlag)
                {
                    //Create the directory
                	debugTextBox.append("Attempting to create Destination Directory...\n");
                	try
                	{
	                	if (destinationDirectory.mkdirs())
	                	{
	                		debugTextBox.append("Destination Directory created\n");
	                		continueFlag = true;
	                	}
	                	else
	                	{
	                		debugTextBox.append("Destination Directory creation failed\nCannot Continue\n");
	                		continueFlag = false;
	                		if (promptFlag)
	                		{
		                		MessageBox mkdirFailMessageBox = new MessageBox(shlDurectiryTimeStamp, SWT.ICON_WARNING | SWT.OK);
		                		mkdirFailMessageBox.setText("Destination Directory Error");
		                		mkdirFailMessageBox.setMessage("Destination Directory creation failed\nCannot Continue\n");
		                        if (mkdirFailMessageBox.open() == SWT.OK)
		                        {                            
		                            debugTextBox.append("Message Result: OK\n");
		                        }
	                		}
	                	}
                	}
                	catch (Exception e)
                	{
                		debugTextBox.append("Destination Directory creation failed\nCannot Continue\n");
                		continueFlag = false;
                		if (promptFlag)
                		{
	                		MessageBox mkdirFailMessageBox = new MessageBox(shlDurectiryTimeStamp, SWT.ICON_WARNING | SWT.OK);
	                		mkdirFailMessageBox.setText("Destination Directory Error");
	                		mkdirFailMessageBox.setMessage("Destination Directory creation failed\nCannot Continue\n");
	                        if (mkdirFailMessageBox.open() == SWT.OK)
	                        {                            
	                            debugTextBox.append("Message Result: OK\n");
	                        }
                		}
                		return false;
                		
                	}
                    
                }
                else
                {
                    //Destination Directory Doesn't Exist, cannot continue
                	debugTextBox.append("Destination Directory doesn't exist. \nCannot continue.\n");
                    if (promptFlag)
                    {
                        MessageBox destDirFailMessageBox = new MessageBox(shlDurectiryTimeStamp, SWT.ICON_WARNING | SWT.OK);
                        destDirFailMessageBox.setText("Destination Directory Error");
                        destDirFailMessageBox.setMessage("Destination Directory doesn't exist.\nCannot Continue\n");
                        if (destDirFailMessageBox.open() == SWT.OK)
                        {                            
                            debugTextBox.append("Message Result: OK\n");
                        }                     
                    }
                    continueFlag = false;
                }
            }
        }
        else
        {
            //Source Directory Doesn't Exist, cannot continue
        	debugTextBox.append("Source Path:" + sourcePathString + "\nSource = Bad Path \n");
            continueFlag = false;

            debugTextBox.append("Source Directory doesn't exist. \nCannot continue.\n");

            if (promptFlag)
            {

                MessageBox sourceDirFailMessageBox = new MessageBox(shlDurectiryTimeStamp, SWT.ICON_WARNING | SWT.OK);
                sourceDirFailMessageBox.setText("Source Directory Error");
                sourceDirFailMessageBox.setMessage("Source Directory doesn't exist.\nCannot Continue\n");
                sourceDirFailMessageBox.open();
                if (sourceDirFailMessageBox.open() == SWT.OK)
                {                            
                    debugTextBox.append("Message Result: OK\n");
                } 
            }
        }

        return continueFlag;



    }
    
    private void sortFiles (String sourcePathString, String destinationPathString) 
    {
    	debugTextBox.append("Copying files...\n");
    	//Get File List
    	File sourceFolder = new File(sourcePathString + "\\");
    	File[] fileCopyList = sourceFolder.listFiles();
        if (deBugTimeFlag)
        {
            debugTextBox.append("Source Path String: " + sourcePathString + "\n");
            debugTextBox.append("Destination Path String: " + destinationPathString + "\n");
            debugTextBox.append("Files In Source Path: " + sourceFolder.length() + "\n");
        }
        
        for (File copyFile : fileCopyList)
        {
        	if (copyFile.isFile())
        	{
	            if (deBugTimeFlag)
	            {
	                debugTextBox.append("First Marker\n");
	            }
	            //Get Last Modified Time of File
	            //DateTime fileLastModifiedDateTime = System.IO.File.GetLastWriteTime(copyFile);
	            String fileYearString = new SimpleDateFormat("yyyy").format(copyFile.lastModified());
	            String fileMonthString = new SimpleDateFormat("MM").format(copyFile.lastModified());
	            String fileDayString =  new SimpleDateFormat("dd").format(copyFile.lastModified());
	            if (deBugTimeFlag)
	            {
	                debugTextBox.append(fileYearString + "-" + fileMonthString + "-" + fileDayString + "\n");
	            }
	
	
	            //Create Directory Structure
	            String extendedDestinationPath =destinationPathString + '\\' + fileYearString;
	
		            if ((sortByChar == 'm') || (sortByChar == 'd'))
		            {
		                extendedDestinationPath = extendedDestinationPath + '\\' + fileMonthString;
		            }
		
		            if ((sortByChar == 'm') || (sortByChar == 'd'))
		            {
		                extendedDestinationPath = extendedDestinationPath + '\\' + fileDayString;
		            }
		
		            //Check if directory already exists
		            File extendedDestinationDirectory = new File(extendedDestinationPath);
		            if ((!extendedDestinationDirectory.exists()) && (!extendedDestinationDirectory.isDirectory()))	            
		            {
		                //If not, we create the directory
		            	try
		            	{
			            	if (extendedDestinationDirectory.mkdirs())
		                	{
			            		debugTextBox.append("Directory Created: " + extendedDestinationPath + "\n");
		                	}
		                	else
		                	{
		                		debugTextBox.append("Could Not Create: " + extendedDestinationPath + "\n");
		                		debugTextBox.append("Destination Directory creation failed\nCannot Continue\n");
		                		if (promptFlag)
		                		{
			                		MessageBox mkdirFailMessageBox = new MessageBox(shlDurectiryTimeStamp, SWT.ICON_WARNING | SWT.OK);
			                		mkdirFailMessageBox.setText("Destination Directory Error");
			                		mkdirFailMessageBox.setMessage("Destination Directory creation failed\nCannot Continue\n");
			                        if (mkdirFailMessageBox.open() == SWT.OK)
			                        {                            
			                            debugTextBox.append("Message Result: OK\n");
			                        }
			                        
		                		}
		                		return;
		                	}
		            	}
		            	catch (Exception e) 
		            	{
	                		debugTextBox.append("Could Not Create: " + extendedDestinationPath + "\n");
	                		debugTextBox.append("Destination Directory creation failed\nCannot Continue\n");
	                		if (promptFlag)
	                		{
		                		MessageBox mkdirFailMessageBox = new MessageBox(shlDurectiryTimeStamp, SWT.ICON_WARNING | SWT.OK);
		                		mkdirFailMessageBox.setText("Destination Directory Error");
		                		mkdirFailMessageBox.setMessage("Destination Directory creation failed\nCannot Continue\n");
		                        if (mkdirFailMessageBox.open() == SWT.OK)
		                        {                            
		                            debugTextBox.append("Message Result: OK\n");
		                        }
		                        
	                		}
	                		return;
		            	}
		                
		            }
		
		
		            //Copy Files
		
		            //Check if file exists already
		            Boolean conflictFlag = false;
		            
		            String copyFileName = copyFile.getName();
	
		            File destination = new File (extendedDestinationPath+"\\" + copyFileName);    //Build the file name from our new destination and the original file name
		            if ((destination.exists()))	
		            {
		                //If the file already exists, check to see what we do
		                conflictFlag = true;
		            }
		
		            Boolean overwriteNextFlag = overwriteFlag;
		            if (conflictFlag && promptFlag)
		            {
		            	
	                	MessageBox mkDupMessageBox = new MessageBox(shlDurectiryTimeStamp, SWT.ICON_WARNING | SWT.YES | SWT.NO);
	                	mkDupMessageBox.setText("File Already Exists");
	                	mkDupMessageBox.setMessage("File Already Exists.\nOverwrite (Yes) or Make Duplicate (No)");
	                                       
	                    if (mkDupMessageBox.open() == SWT.YES)
	                    {
	                    	overwriteNextFlag = true;
	                    	debugTextBox.append("File Already Exists:" + destination + "\nOverwrite: Yes \n");
	                    }
	                    else
	                    {
	                    	overwriteNextFlag = false;
	                    	debugTextBox.append("File Already Exists:" + destination + "\nOverwrite: No \n");
	                    }
		
		            }
		
		            //If we don't overwrite existing files, then we make a duplicate with a unique name
		            if (conflictFlag && !overwriteNextFlag)
		            {
		                int fileVersion = 0;
		                while ((destination.exists()) && (fileVersion < 255))
		                {
		                    destination = new File (extendedDestinationPath + "\\(" + fileVersion  + ")" + copyFileName);
		                    fileVersion = fileVersion + 1;
		                }
		            }
		
		            //Finally, copy the file
		            if (deBugTimeFlag)
		            {
		                debugTextBox.append("Copy File: " + copyFile + "\n");
		                debugTextBox.append("Destination File: " + destination + "\n");
		            }
		            try
		            {	            	
		            	Files.copy(copyFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
			            if (deBugTimeFlag)
			            {
			                debugTextBox.append("Source: " + copyFile + "\nDestination: " + destination + "\n");
			            }
		            }
		            catch (Exception e) 
		            {
		                System.out.println(e.toString());
                    	debugTextBox.append("Copy Failed: " + copyFile + "\n");
                    	if (promptFlag)
                    	{
                    		MessageBox copyFailMessageBox = new MessageBox(shlDurectiryTimeStamp, SWT.ICON_WARNING | SWT.OK);
                        	copyFailMessageBox.setText("Copy Failed");
                        	copyFailMessageBox.setMessage("Copy Failed: " + copyFile + "\n");
                            if (copyFailMessageBox.open() == SWT.OK)
                            {                            
                                debugTextBox.append("Copy Failed: OK\n Cannot Continue\n");
                            }                    		
                    	}

                        return;
		            }
        		}
	
	
	        }
	        debugTextBox.append("File Copy Complete\n");
	
	
	
	        //Remove Files if Move Option is True
	        if (moveFlag)
	        {
	            debugTextBox.append("Removing Source Files...\n");
	            for (File removeFile : fileCopyList)
	            {
	                if (removeFile.exists())
	                {
	                	try 
	                	{
		                    if (removeFile.delete())
		                    {
		                        if (deBugTimeFlag)
		                        {
		                            debugTextBox.append("Deleted: " + removeFile + "\n");
		                        }
		                    }
		                    else  
		    	            {
		                    	debugTextBox.append("Delete Failed: " + removeFile + "\n");
		                    	MessageBox deleteFailMessageBox = new MessageBox(shlDurectiryTimeStamp, SWT.ICON_WARNING | SWT.OK);
		                    	deleteFailMessageBox.setText("Delete Failed");
		        	            deleteFailMessageBox.setMessage("Delete Failed: " + removeFile + "\n");
		                        if (deleteFailMessageBox.open() == SWT.OK)
		                        {                            
		                            debugTextBox.append("Delete Failed: OK\n Cannot Continue\n");
		                        }
		                    	
		    	            }
	                	}
	                	catch (Exception e)
	                	{
	                		System.out.println(e.toString());
	                    	debugTextBox.append("Delete Failed: " + removeFile + "\n");
	                    	if (promptFlag)
	                    	{
		                    	MessageBox deleteFailMessageBox = new MessageBox(shlDurectiryTimeStamp, SWT.ICON_WARNING | SWT.OK);
		                    	deleteFailMessageBox.setText("Delete Failed");
		        	            deleteFailMessageBox.setMessage("Delete Failed: " + removeFile + "\n");
		                        if (deleteFailMessageBox.open() == SWT.OK)
		                        {                            
		                            debugTextBox.append("Delete Failed: OK\n Cannot Continue\n");
		                        }
	                    	}
	                        return;
	                		
	                	}
                	
	                }
	            }
	            debugTextBox.append("Removal Complete\n");
	        }
	
	
	        //The end
	        debugTextBox.append("Process Finished \n");
	        if (promptFlag)
	        {
	        	String caption = "";
	            if (moveFlag)
	            {
	                caption = "All Files Copied and Moved";
	            }
	            else
	            {
	                caption = "All Files Copied";
	            }
	            
	            MessageBox finishedMessageBox = new MessageBox(shlDurectiryTimeStamp, SWT.ICON_WARNING | SWT.OK);
	            finishedMessageBox.setText("Process Finish");
	            finishedMessageBox.setMessage("Process Finished: " + caption + "\n");
                if (finishedMessageBox.open() == SWT.OK)
                {                            
                    debugTextBox.append("Process Finished: OK\n");
                }
	
	        }
    }
}
