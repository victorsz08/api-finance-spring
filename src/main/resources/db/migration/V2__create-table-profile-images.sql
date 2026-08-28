

CREATE TABLE profile_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_image_url VARCHAR(255),
    user_id UUID,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE  
);