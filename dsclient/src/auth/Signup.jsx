import { useState } from "react";
import axios from "axios";
import {useNavigate} from "react-router-dom";

const Login = () => {
    const navigate = useNavigate();

    const [input, setInput] = useState({
        email: "",
        username: "",
        password: "",
    });

    const handleSubmitEvent = async (e) => {
        e.preventDefault();
        console.log("email: ", input.email);
        console.log("username: ", input.username);
        console.log("password: ", input.password);
        if (input.username !== "" && input.password !== "") {
            await axios({
                method: 'post',
                url: `/api/users/signup`,
                data: {
                    email: input.email,
                    username: input.username,
                    password: input.password
                },
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json'
                }
            }).then(
                (response) => {
                    navigate("/login");
                }
            ).catch(
                (err) => console.error(err)
            );
        } else {
            alert("please provide a valid input");
        }
    };

    const handleInput = (e) => {
        const { name, value } = e.target;
        setInput((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    return (
        <form onSubmit={handleSubmitEvent}>
            <div className="form_control">
                <label htmlFor="user-email">Email:</label>
                <input
                    type="email"
                    id="user-email"
                    name="email"
                    placeholder="email@email.com"
                    aria-describedby="user-email"
                    aria-invalid="false"
                    onChange={handleInput}
                />
                <div id="user-email" className="sr-only">
                    Please enter a valid username. It must contain at least 6 characters.
                </div>
            </div>
            <div className="form_control">
                <label htmlFor="username">Username:</label>
                <input
                    type="text"
                    id="username"
                    name="username"
                    placeholder="username"
                    aria-describedby="username"
                    aria-invalid="false"
                    onChange={handleInput}
                />
                <div id="username" className="sr-only">
                    your username should be more than 6 character
                </div>
            </div>
            <div className="form_control">
                <label htmlFor="password">Password:</label>
                <input
                    type="password"
                    id="password"
                    name="password"
                    aria-describedby="user-password"
                    aria-invalid="false"
                    onChange={handleInput}
                />
                <div id="user-password" className="sr-only">
                    your password should be more than 6 character
                </div>
            </div>
            <button className="btn-submit">Submit</button>
        </form>
    );
};

export default Login;
