package com.rxlogix.cmis

class AdapterFactory {
    public static AdapterInterface getAdapter(def settings) {

        AdapterInterface adapter
        // add other adapter implementations if needed
        if(settings.api) {
            adapter = new ApiAdapter()
        } else {
            adapter = new DefaultAdapter()
        }
        adapter.init(settings)
        return adapter
    }
}
