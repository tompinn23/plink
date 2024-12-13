package org.int13h.plink.server;


public interface HttpResponse {

    void status(HttpStatus status);

    void text(CharSequence cs);
    void html(CharSequence cs);

    void write(CharSequence cs);
}
