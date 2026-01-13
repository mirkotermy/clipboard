package com.example.demo;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class KeyList{
    private String currUser;
    private boolean ctrlpressed = false;
    private FirebaseDatabase db;
    private DatabaseReference ref;
    private volatile String  lastext;
    private String img_path;
    private volatile int lastimg;
    private int currImage;
    public KeyList(String currUser){
        this.currUser = currUser;
        db =  FirebaseDatabase.getInstance();
        currImage = -1;
        lastimg = 0;
    }

        public void monitorClipboard(){
            lastext = "";
            Clipboard text = Toolkit.getDefaultToolkit().getSystemClipboard();
            new Thread(() ->{
                String testo;
                
                while(true){
                    try {
                        if(text.isDataFlavorAvailable(DataFlavor.imageFlavor)){
                            Image image = (Image) text.getData(DataFlavor.imageFlavor);
                            String base64 = "";
                            if(image != null && currImage != lastimg){
                                System.out.println("Immagine rilevata");
                                BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
                                bufferedImage.getGraphics().drawImage(image, 0,0,null);
                                bufferedImage.getGraphics().dispose();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ImageIO.write(bufferedImage, "jpg", baos); //Scrive l'immagine all'interno dell array di byte
                                byte[] imageBytes = baos.toByteArray();
                                base64 = Base64.getEncoder().encodeToString(imageBytes);
                                db.getReference("utenti").child(currUser).child("img_path").setValueAsync("data:image/png;base64,"+base64).get(); 
                            }
                                ImageSelection selection = new ImageSelection(null); 
                                text.setContents(selection, null);
                                //CONTROLLO se l'immagine che ho inviato è dallo stesso dispositivo o no
                                if(base64 != null && base64.isEmpty()){
                                    lastimg = base64.hashCode(); // se l'immagine non è quella che ho mandato io
                                }else{
                                    lastimg = 0; // se è quella che ho mandato io
                                }
                           
                        }else if(text.isDataFlavorAvailable(DataFlavor.stringFlavor)){
                            testo = (String) text.getData(DataFlavor.stringFlavor);
                            if(text != null && !lastext.equals(testo)){ 
                                db.getReference("utenti").child(currUser).child("clipboard").setValueAsync(testo);
                                lastext = testo;
                            }      
                        }
                                    
                    } catch (IllegalStateException e) {
                    // La clipboard è momentaneamente occupata da un'altra app.
                    // È normale, ignoriamo e riproviamo al prossimo ciclo.
                    } catch (UnsupportedFlavorException | IOException e) {
                    // Gestione errori di lettura generici
                        e.printStackTrace();
                    } catch (Exception e) {
                    // Catch-all per evitare che il Thread muoia improvvisamente
                        System.err.println("Errore imprevisto nel monitor clipboard: " + e.getMessage());
                }

                 try {
                    Thread.sleep(1000); // Controlla ogni 1 secondo (1000ms)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break; // Interrompe il ciclo se il thread viene fermato
                }
                }
            }).start();
        }
        
            public void monitorFirebase(){
                            db.getReference("utenti").child(currUser).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot){
                                    if(!dataSnapshot.child("clipboard").getValue(String.class).equals(lastext) && !dataSnapshot.child("clipboard").getValue(String.class).isEmpty()&& (dataSnapshot.child("clipboard").getValue(String.class) != null)){
                                        String copy = dataSnapshot.child("clipboard").getValue().toString();
                                         lastext = copy;
                                         //Siccome firebase ha un proprio thread quindi se provassi a eseguire altro al di fuori delle sue funzionalità crasherebbe
                                         //poichè la clipboard appartiene al thread principale vanno in conflitto
                                        SwingUtilities.invokeLater(()->{
                                            Clipboard text = Toolkit.getDefaultToolkit().getSystemClipboard();  
                                            StringSelection stringtext = new StringSelection(copy);
                                            text.setContents(stringtext, null);
                                        });
                                    }
                                    String img = dataSnapshot.child("img_path").getValue(String.class);
                                    //db.getReference("utenti").child(currUser).child("img_path").setValueAsync("");
                                    if(img != null && !img.isEmpty()){
                                        String contacaratteri = "data:image/png;base64,";
                                        db.getReference("utenti").child(currUser).child("img_path").setValueAsync("");
                                        img_path = img.substring(contacaratteri.length(), img.length());
                                        currImage = img_path.hashCode();
                                    }
                                    System.out.println("INVIATA "+currImage + "---" + lastimg);
                                    if(lastimg != currImage && !dataSnapshot.child("img_path").getValue(String.class).isEmpty()){
                                        System.out.print("Immagine estratta");                                        
                                        byte[] image = Base64.getDecoder().decode(img_path);
                                        if(image != null){
                                           Image convertImage = new ImageIcon(image).getImage(); 
                                           lastimg = currImage;                                          
                                           SwingUtilities.invokeLater(()->{
                                            Clipboard text = Toolkit.getDefaultToolkit().getSystemClipboard();
                                            ImageSelection selection = new ImageSelection(convertImage); 
                                            text.setContents(selection, null);
                                        });
                                        }
                                        
                                    }
                                    
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        throw new UnsupportedOperationException("Not supported yet.");
                                    }
                                });
                            }
                            
    class ImageSelection implements Transferable {
        private Image image;

        public ImageSelection(Image image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }               
    }
}



