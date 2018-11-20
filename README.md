# MultiIndicator(粗糙版本)

1. Add it in your root build.gradle at the end of repositories:

    	allprojects {
		    repositories {
			    ...
			    maven { url 'https://jitpack.io' }
		    }
	    }
      
      
2.  Add the dependency

    	dependencies {
	        implementation 'com.github.kaycool:MultiIndicator:Tag'
    	}
       
3. add tag to xml

    	<com.kai.wang.space.indicator.lib.MultiFlowIndicator
            	android:id="@+id/spaceFlowIndicator"
            	app:si_max_height="150dp"
            	android:layout_width="match_parent"
            	android:layout_height="wrap_content"/>

4. define title list to activity

        private val mTitles by lazy {     mutableListOf()}

5. viewpager set adapter

    	 viewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
           	 override fun getItem(p0: Int): Fragment {
               	 	return TestFragment()
            	}

            	override fun getCount(): Int {
                	return mTitles.size
            	}

            	override fun getPageTitle(position: Int): CharSequence? {
                	return mTitles[position]
            	}

        	}

6. MultiFlowIndicator setAdapter ,object who is implements MultiFlowAdapter<T>

            spaceFlowIndicator.setAdapter(object : MultiFlowAdapter<Any> {
            override fun getItemCount(): Int {
                return mTitles.size
            }

            override fun getView(parent: MultiFlowIndicator, position: Int, t: Any): View {
                val textView = TextView(applicationContext)
                textView.text = mTitles[position]
                textView.setPadding(
                    resources.getDimensionPixelOffset(R.dimen.dimen_8)
                    , resources.getDimensionPixelOffset(R.dimen.dimen_5)
                    , resources.getDimensionPixelOffset(R.dimen.dimen_8)
                    , resources.getDimensionPixelOffset(R.dimen.dimen_5)
                )
                return textView
            }

            override fun onSelected(view: View, position: Int) {
                (view as? TextView)?.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        android.R.color.holo_red_light
                    )
                )

            }

            override fun unSelected(view: View, position: Int) {
                (view as? TextView)?.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        android.R.color.black
                    )
                )
            }

            override fun getItem(position: Int): Any {
                return mTitles[position]
            }

        })
        
7. setViewPager

     	spaceFlowIndicator.setViewPager(viewPager)

