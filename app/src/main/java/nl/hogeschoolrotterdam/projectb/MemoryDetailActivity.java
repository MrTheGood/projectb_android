package nl.hogeschoolrotterdam.projectb;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import nl.hogeschoolrotterdam.projectb.data.Database;
import nl.hogeschoolrotterdam.projectb.data.Memory;

public class MemoryDetailActivity extends AppCompatActivity {
    Intent shareIntent;
    Memory memory;
    ViewPager2 viewPager2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(WhibApp.getInstance().getThemeId());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_detail);
        viewPager2 = findViewById(R.id.viewPager2);

        viewPager2.setAdapter(new ViewPagerAdapter());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Drawable homeAsUpIndicator = ContextCompat.getDrawable(this, R.drawable.ic_action_close); // Workaround for a bug in MaterialComponents
        getSupportActionBar().setHomeAsUpIndicator(WhibApp.getInstance().tintDrawable(homeAsUpIndicator));

        TextView memoryTitleTextView = findViewById(R.id.memoryTitleTextView);
        TextView memoryDatetextView = findViewById(R.id.memoryDatetextView);
        TextView memoryDescriptionTextView = findViewById(R.id.memoryDescriptionTextView);
        ImageView imageView = findViewById(R.id.imageView);
        //change.
        Database database = Database.getInstance();
        String sessionId = getIntent().getStringExtra("EXTRA_SESSION_ID");
        memory = database.findMemory(sessionId);

        // showing content (images not included in demo content)
        memoryDatetextView.setText(memory.getDateText());
        memoryTitleTextView.setText(memory.getTitle());
        memoryDescriptionTextView.setText(memory.getDescription());
        //imageView.setImageDrawable(memory.getThumbnail().getImage()); // for thumbnail in list
        // if (media instanceOf Image) imageView.setImageDrawable(media.getImage()); // for image in swipable detail list

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main_memorydetail_top);
        WhibApp.getInstance().tintMenuItems(menu); // Workaround for a Material Components bug

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.shareBtn:
                Uri imageUri = Uri.parse("android.resource://" + getPackageName()
                        + "/drawable/" + "ic_launcher");
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, memory.getTitle() + "\n" + memory.getDescription());
                if (memory.getMedia().size()>0) {
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    shareIntent.setType("image/jpeg");
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }else{
                    shareIntent.setType("text/plain");
                }
                startActivity(Intent.createChooser(shareIntent, "How would you like to share this memory?"));
                //shareIntent = new Intent(Intent.ACTION_SEND);
                //shareIntent.setType("text/plain");
                //shareIntent.putExtra(Intent.EXTRA_SUBJECT,memory.getTitle());
                //shareIntent.putExtra(Intent.EXTRA_TEXT, memory.getDescription());
                //startActivity(Intent.createChooser(shareIntent, "Share memories via"));
                return true;
            case R.id.deleteBtn:
                new AlertDialog.Builder(MemoryDetailActivity.this)
                        .setTitle("Delete Memory")
                        .setMessage("Are you sure you want to delete this memory? This action cannot be undone.")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                //todo: implement delete
                                // SQLiteDatabase db = this.getWritableDatabase();
                                // return db.delete(TABLE_NAME, "ID =?", new String[]{id});
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            case R.id.editBtn:
                Toast.makeText(this, "Action Edit selected", Toast.LENGTH_LONG).show();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
