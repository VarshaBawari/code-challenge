package projects.varsha.com.babblegame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDescriptionComponents();
    }

    private void initDescriptionComponents() {

        this.findViewById(R.id.welcome_container).setVisibility(View.VISIBLE);

        this.findViewById(R.id.button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent k = new Intent(MainActivity.this, GameActivity.class);
                    startActivity(k);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }
}
